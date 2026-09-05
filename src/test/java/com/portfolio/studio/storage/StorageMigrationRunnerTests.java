package com.portfolio.studio.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.portfolio.studio.config.PortfolioProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StorageMigrationRunnerTests {

    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @TempDir
    Path tempDir;

    private LocalDiskObjectStore localDiskObjectStore;
    private MinioObjectStore minioObjectStore;
    private PortfolioProperties portfolioProperties;

    @BeforeEach
    void setUp() {
        localDiskObjectStore = new LocalDiskObjectStore(tempDir);
        minioObjectStore = mock(MinioObjectStore.class);
        portfolioProperties = new PortfolioProperties();
        portfolioProperties.getStorage().setUploadRoot(tempDir.toString());
    }

    @Test
    void copyUploadsMissingKeysAndKeepsDiskFiles() throws Exception {
        Path filePath = tempDir.resolve("gallery/keep.png");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, PIXEL_PNG);
        when(minioObjectStore.exists("gallery/keep.png")).thenReturn(false);

        runner(true, false).run(null);

        verify(minioObjectStore).put(eq("gallery/keep.png"), any(), eq((long) PIXEL_PNG.length), anyString());
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    void copySkipsKeysAlreadyInMinio() throws Exception {
        Path filePath = tempDir.resolve("gallery/existing.png");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, PIXEL_PNG);
        when(minioObjectStore.exists("gallery/existing.png")).thenReturn(true);

        runner(true, false).run(null);

        verify(minioObjectStore, never()).put(anyString(), any(), anyLong(), anyString());
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    void deleteRemovesDiskOnlyWhenMinioHasTheObject() throws Exception {
        Path present = tempDir.resolve("gallery/copied.png");
        Path missing = tempDir.resolve("gallery/local-only.png");
        Files.createDirectories(present.getParent());
        Files.write(present, PIXEL_PNG);
        Files.write(missing, PIXEL_PNG);
        when(minioObjectStore.exists("gallery/copied.png")).thenReturn(true);
        when(minioObjectStore.exists("gallery/local-only.png")).thenReturn(false);

        runner(false, true).run(null);

        assertThat(Files.exists(present)).isFalse();
        assertThat(Files.exists(missing)).isTrue();
    }

    @Test
    void refusesWhenFlagsOnButMinioMissing() {
        portfolioProperties.getStorage().getS3().setMigrate(true);
        StorageMigrationRunner runner = new StorageMigrationRunner(
            portfolioProperties,
            localDiskObjectStore,
            emptyProvider()
        );

        assertThatThrownBy(() -> runner.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not fully configured");
    }

    private StorageMigrationRunner runner(boolean migrate, boolean deleteAfterVerify) {
        portfolioProperties.getStorage().getS3().setMigrate(migrate);
        portfolioProperties.getStorage().getS3().setDeleteLocalAfterVerify(deleteAfterVerify);
        return new StorageMigrationRunner(portfolioProperties, localDiskObjectStore, provider(minioObjectStore));
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<MinioObjectStore> provider(MinioObjectStore store) {
        ObjectProvider<MinioObjectStore> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(store);
        return objectProvider;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<MinioObjectStore> emptyProvider() {
        ObjectProvider<MinioObjectStore> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(null);
        return objectProvider;
    }
}
