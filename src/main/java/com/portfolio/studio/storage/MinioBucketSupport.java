package com.portfolio.studio.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public final class MinioBucketSupport {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketSupport.class);

    private MinioBucketSupport() {
    }

    /**
     * Verifies that the bucket exists, creating it when missing.
     *
     * <p>Unreachable MinIO does not abort startup. Callers keep the local-disk store as the
     * write fallback until MinIO recovers.</p>
     *
     * @param client the MinIO client
     * @param bucket the bucket name
     * @return {@code true} if the bucket exists or was created; {@code false} if the name is blank
     *         or MinIO cannot be reached
     */
    public static boolean ensureBucket(MinioClient client, String bucket) {
        if (!StringUtils.hasText(bucket)) {
            return false;
        }
        String name = bucket.trim();
        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            }
            return true;
        } catch (Exception exception) {
            log.warn(
                "MinIO is configured but unreachable for bucket '{}'; continuing with local disk uploads.",
                name,
                exception
            );
            return false;
        }
    }
}
