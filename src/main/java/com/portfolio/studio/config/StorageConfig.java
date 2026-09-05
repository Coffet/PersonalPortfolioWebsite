package com.portfolio.studio.config;

import java.net.URI;
import java.nio.file.Paths;

import com.portfolio.studio.storage.LocalDiskObjectStore;
import com.portfolio.studio.storage.MinioObjectStore;
import com.portfolio.studio.storage.S3FullyConfiguredCondition;
import com.portfolio.studio.storage.S3StorageSupport;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Bean
    LocalDiskObjectStore localDiskObjectStore(PortfolioProperties portfolioProperties) {
        return new LocalDiskObjectStore(
            Paths.get(portfolioProperties.getStorage().getUploadRoot()).toAbsolutePath().normalize()
        );
    }

    @Bean
    @Conditional(S3FullyConfiguredCondition.class)
    MinioClient minioClient(PortfolioProperties portfolioProperties) {
        PortfolioProperties.Storage.S3 s3 = portfolioProperties.getStorage().getS3();
        URI endpoint = S3StorageSupport.requireAllowedEndpoint(s3.getEndpoint());
        boolean secure = "https".equalsIgnoreCase(endpoint.getScheme());
        int port = endpoint.getPort();
        if (port < 0) {
            port = secure ? 443 : 80;
        }
        MinioClient.Builder builder = MinioClient.builder()
            .endpoint(endpoint.getHost(), port, secure)
            .credentials(s3.getAccessKey(), s3.getSecretKey());
        if (StringUtils.hasText(s3.getRegion())) {
            builder.region(s3.getRegion().trim());
        }
        MinioClient client = builder.build();
        ensureBucket(client, s3.getBucket().trim());
        return client;
    }

    @Bean
    @Conditional(S3FullyConfiguredCondition.class)
    MinioObjectStore minioObjectStore(MinioClient minioClient, PortfolioProperties portfolioProperties) {
        String bucket = portfolioProperties.getStorage().getS3().getBucket().trim();
        log.info("MinIO object store enabled for bucket '{}'", bucket);
        return new MinioObjectStore(minioClient, bucket);
    }

    private static void ensureBucket(MinioClient client, String bucket) {
        Exception last = null;
        long delayMs = 500L;
        for (int attempt = 1; attempt <= 8; attempt++) {
            try {
                boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!found) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
                return;
            } catch (Exception exception) {
                last = exception;
                if (attempt == 8) {
                    break;
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for MinIO.", interrupted);
                }
                delayMs = Math.min(delayMs * 2, 4000L);
            }
        }
        throw new IllegalStateException("Unable to reach MinIO or create the configured bucket.", last);
    }
}
