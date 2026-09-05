package com.portfolio.studio.config;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.portfolio.studio.storage.LocalDiskObjectStore;
import com.portfolio.studio.storage.MinioObjectStore;
import com.portfolio.studio.storage.S3FullyConfiguredCondition;
import com.portfolio.studio.storage.S3StorageSupport;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
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
        int port = endpoint.getPort() < 0 ? 443 : endpoint.getPort();
        MinioClient.Builder builder = MinioClient.builder()
            .endpoint(endpoint.getHost(), port, true)
            .credentials(s3.getAccessKey(), s3.getSecretKey());
        if (StringUtils.hasText(s3.getTrustCert())) {
            builder.httpClient(httpClientTrusting(Paths.get(s3.getTrustCert().trim())));
        }
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

    private static OkHttpClient httpClientTrusting(Path certPath) {
        if (!Files.isRegularFile(certPath)) {
            throw new IllegalStateException(
                "portfolio.storage.s3.trust-cert is set but the file is missing."
            );
        }
        try (InputStream inputStream = Files.newInputStream(certPath)) {
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("minio", certificate);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(keyStore);
            X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .build();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load portfolio.storage.s3.trust-cert.", exception);
        }
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
            }
            if (attempt < 8) {
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
