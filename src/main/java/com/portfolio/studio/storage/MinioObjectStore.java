package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.MinioClient;
import org.springframework.util.StringUtils;

public class MinioObjectStore implements ObjectStore {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioObjectStore(MinioClient minioClient, String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public void put(String key, InputStream content, long contentLength, String contentType) throws IOException {
        String valid = ObjectKeyValidator.requireValid(key);
        String type = StringUtils.hasText(contentType) ? contentType : ContentTypeResolver.fromKey(valid);
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(valid)
                    .stream(content, contentLength, -1L)
                    .contentType(type)
                    .build()
            );
        } catch (Exception exception) {
            throw wrap("Unable to store object in MinIO.", exception);
        }
    }

    @Override
    public Optional<StoredObject> get(String key) throws IOException {
        String valid = ObjectKeyValidator.requireValid(key);
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket).object(valid).build()
            );
            GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(valid).build()
            );
            String contentType = StringUtils.hasText(stat.contentType())
                ? stat.contentType()
                : ContentTypeResolver.fromKey(valid);
            return Optional.of(new StoredObject(response, stat.size(), contentType));
        } catch (ErrorResponseException exception) {
            if (isMissingObject(exception)) {
                return Optional.empty();
            }
            throw wrap("Unable to read object from MinIO.", exception);
        } catch (Exception exception) {
            throw wrap("Unable to read object from MinIO.", exception);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        String valid = ObjectKeyValidator.requireValid(key);
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(valid).build());
            return true;
        } catch (ErrorResponseException exception) {
            if (isMissingObject(exception)) {
                return false;
            }
            throw wrap("Unable to check object in MinIO.", exception);
        } catch (Exception exception) {
            throw wrap("Unable to check object in MinIO.", exception);
        }
    }

    @Override
    public void deleteIfPresent(String key) throws IOException {
        Optional<String> parsed = ObjectKeyValidator.parse(key);
        if (parsed.isEmpty()) {
            return;
        }
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket).object(parsed.get()).build()
            );
        } catch (ErrorResponseException exception) {
            if (isMissingObject(exception)) {
                return;
            }
            throw wrap("Unable to delete object from MinIO.", exception);
        } catch (Exception exception) {
            throw wrap("Unable to delete object from MinIO.", exception);
        }
    }

    public boolean hasMatchingContent(String key, Path localFile) throws IOException {
        if (localFile == null || !Files.isRegularFile(localFile) || !exists(key)) {
            return false;
        }
        String valid = ObjectKeyValidator.requireValid(key);
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket).object(valid).build()
            );
            if (stat.size() != Files.size(localFile)) {
                return false;
            }
            try (GetObjectResponse remote = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(valid).build()
                );
                InputStream local = Files.newInputStream(localFile)) {
                return sha256Hex(local).equalsIgnoreCase(sha256Hex(remote));
            }
        } catch (ErrorResponseException exception) {
            if (isMissingObject(exception)) {
                return false;
            }
            throw wrap("Unable to compare object in MinIO.", exception);
        } catch (Exception exception) {
            throw wrap("Unable to compare object in MinIO.", exception);
        }
    }

    private static String sha256Hex(InputStream content) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = content.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("Missing digest SHA-256", exception);
        }
    }

    private static boolean isMissingObject(ErrorResponseException exception) {
        if (exception.errorResponse() == null) {
            return false;
        }
        String code = exception.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchObject".equals(code);
    }

    private static IOException wrap(String message, Exception exception) {
        if (exception instanceof IOException ioException) {
            return ioException;
        }
        return new IOException(message, exception);
    }
}
