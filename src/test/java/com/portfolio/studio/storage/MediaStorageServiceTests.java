package com.portfolio.studio.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.portfolio.studio.service.MediaStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaStorageServiceTests {

    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @TempDir
    Path tempDir;

    private LocalDiskObjectStore localDiskObjectStore;

    @BeforeEach
    void setUp() {
        localDiskObjectStore = new LocalDiskObjectStore(tempDir);
    }

    @Test
    void storeWritesToDiskWhenMinioIsOff() throws IOException {
        MediaStorageService service = new MediaStorageService(localDiskObjectStore, emptyProvider());
        MockMultipartFile file = png("shot.png");

        MediaStorageService.StoredFile stored = service.store(file, "gallery");

        assertThat(stored.publicPath()).startsWith("/uploads/gallery/");
        String key = stored.publicPath().substring("/uploads/".length());
        assertThat(Files.isRegularFile(tempDir.resolve(key))).isTrue();
    }

    @Test
    void storeWritesToMinioOnlyWhenEnabled() throws IOException {
        MinioObjectStore minioObjectStore = mock(MinioObjectStore.class);
        MediaStorageService service = new MediaStorageService(localDiskObjectStore, provider(minioObjectStore));

        MediaStorageService.StoredFile stored = service.store(png("shot.png"), "gallery");

        String key = stored.publicPath().substring("/uploads/".length());
        verify(minioObjectStore).put(anyString(), any(), anyLong(), anyString());
        assertThat(Files.exists(tempDir.resolve(key))).isFalse();
    }

    @Test
    void storeFailsClearlyWhenMinioPutThrows() throws IOException {
        MinioObjectStore minioObjectStore = mock(MinioObjectStore.class);
        doThrow(new IOException("minio down")).when(minioObjectStore).put(anyString(), any(), anyLong(), anyString());
        MediaStorageService service = new MediaStorageService(localDiskObjectStore, provider(minioObjectStore));

        assertThatThrownBy(() -> service.store(png("shot.png"), "gallery"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unable to store image file");
        assertThat(localDiskObjectStore.listKeys()).isEmpty();
    }

    @Test
    void deleteRemovesFromDiskAndMinioWhenEnabled() throws IOException {
        MinioObjectStore minioObjectStore = mock(MinioObjectStore.class);
        MediaStorageService service = new MediaStorageService(localDiskObjectStore, provider(minioObjectStore));
        Path diskFile = tempDir.resolve("gallery/sample.png");
        Files.createDirectories(diskFile.getParent());
        Files.write(diskFile, PIXEL_PNG);

        service.deleteIfPresent("/uploads/gallery/sample.png");

        assertThat(Files.exists(diskFile)).isFalse();
        verify(minioObjectStore).deleteIfPresent("gallery/sample.png");
    }

    @Test
    void deleteFailsWhenMinioDeleteThrows() throws IOException {
        MinioObjectStore minioObjectStore = mock(MinioObjectStore.class);
        doThrow(new IOException("minio delete failed")).when(minioObjectStore).deleteIfPresent("gallery/sample.png");
        MediaStorageService service = new MediaStorageService(localDiskObjectStore, provider(minioObjectStore));
        Path diskFile = tempDir.resolve("gallery/sample.png");
        Files.createDirectories(diskFile.getParent());
        Files.write(diskFile, PIXEL_PNG);

        assertThatThrownBy(() -> service.deleteIfPresent("/uploads/gallery/sample.png"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unable to delete image file");
        assertThat(Files.exists(diskFile)).isTrue();
    }

    private static MockMultipartFile png(String filename) {
        return new MockMultipartFile("file", filename, "image/png", PIXEL_PNG);
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
