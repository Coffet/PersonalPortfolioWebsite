package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.portfolio.studio.config.PortfolioProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StorageMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StorageMigrationRunner.class);

    private final PortfolioProperties portfolioProperties;
    private final LocalDiskObjectStore localDiskObjectStore;
    private final MinioObjectStore minioObjectStore;

    /**
     * Creates a runner for migrating and optionally removing local storage objects.
     *
     * @param portfolioProperties configuration for storage migration and cleanup
     * @param localDiskObjectStore local object store containing the source files
     * @param minioObjectStoreProvider provider for the optional MinIO object store
     */
    public StorageMigrationRunner(
        PortfolioProperties portfolioProperties,
        LocalDiskObjectStore localDiskObjectStore,
        ObjectProvider<MinioObjectStore> minioObjectStoreProvider
    ) {
        this.portfolioProperties = portfolioProperties;
        this.localDiskObjectStore = localDiskObjectStore;
        this.minioObjectStore = minioObjectStoreProvider.getIfAvailable();
    }

    /**
     * Executes the configured local-storage migration and cleanup operations.
     *
     * @throws IllegalStateException if migration or cleanup is enabled while MinIO is unavailable
     * @throws Exception if a configured storage operation fails
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        PortfolioProperties.Storage.S3 s3 = portfolioProperties.getStorage().getS3();
        boolean migrate = s3.isMigrate();
        boolean deleteAfterVerify = s3.isDeleteLocalAfterVerify();
        if (!migrate && !deleteAfterVerify) {
            return;
        }
        if (minioObjectStore == null) {
            throw new IllegalStateException(
                "portfolio.storage.s3.migrate or delete-local-after-verify is true, but MinIO is not fully configured."
            );
        }
        if (migrate) {
            copyDiskToMinio();
        }
        if (deleteAfterVerify) {
            deleteLocalCopiesPresentInMinio();
        }
    }

    /**
     * Copies local disk objects that are absent from MinIO.
     *
     * <p>Existing MinIO objects are skipped, and local files are preserved after copying.</p>
     *
     * @throws IOException if a local object cannot be read or uploaded
     */
    void copyDiskToMinio() throws IOException {
        List<String> keys = localDiskObjectStore.listKeys();
        int copied = 0;
        int skipped = 0;
        for (String key : keys) {
            if (minioObjectStore.exists(key)) {
                skipped++;
                continue;
            }
            Path filePath = localDiskObjectStore.getUploadRoot().resolve(key).normalize();
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                minioObjectStore.put(key, inputStream, Files.size(filePath), ContentTypeResolver.fromKey(key));
            }
            copied++;
        }
        log.info("MinIO migrate copied {} object(s); skipped {} already present. Disk files were kept.", copied, skipped);
    }

    /**
     * Deletes local object files whose content matches the corresponding MinIO objects.
     *
     * @throws IOException if local object keys cannot be listed or a matching local file cannot be deleted
     */
    void deleteLocalCopiesPresentInMinio() throws IOException {
        List<String> keys = localDiskObjectStore.listKeys();
        int deleted = 0;
        int kept = 0;
        for (String key : keys) {
            Path filePath = localDiskObjectStore.getUploadRoot().resolve(key).normalize();
            if (minioObjectStore.hasMatchingContent(key, filePath)) {
                localDiskObjectStore.deleteIfPresent(key);
                deleted++;
            } else {
                kept++;
            }
        }
        log.info(
            "MinIO delete-local-after-verify removed {} disk file(s); kept {} missing or not matching MinIO content.",
            deleted,
            kept
        );
    }
}
