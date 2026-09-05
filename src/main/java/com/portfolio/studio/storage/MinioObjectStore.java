package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
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
    public void deleteIfPresent(String key) {
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
            // A missing file should not block content deletion.
        } catch (Exception ignored) {
            // A missing file should not block content deletion.
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
        if (exception instanceof InvalidKeyException
            || exception instanceof NoSuchAlgorithmException
            || exception instanceof InsufficientDataException
            || exception instanceof InternalException
            || exception instanceof InvalidResponseException
            || exception instanceof ServerException
            || exception instanceof XmlParserException
            || exception instanceof ErrorResponseException) {
            return new IOException(message, exception);
        }
        return new IOException(message, exception);
    }
}
