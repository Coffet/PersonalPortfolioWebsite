package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
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
            String etag = normalizeEtag(stat.etag());
            if (etag != null && !etag.contains("-")) {
                return etag.equalsIgnoreCase(md5Hex(localFile));
            }
            return sha256Hex(localFile).equalsIgnoreCase(sha256Hex(valid));
        } catch (ErrorResponseException exception) {
            if (isMissingObject(exception)) {
                return false;
            }
            throw wrap("Unable to compare object in MinIO.", exception);
        } catch (Exception exception) {
            throw wrap("Unable to compare object in MinIO.", exception);
        }
    }

    private static String normalizeEtag(String etag) {
        if (!StringUtils.hasText(etag)) {
            return null;
        }
        return etag.replace("\"", "").trim();
    }

    private static String md5Hex(Path localFile) throws IOException {
        return digestHex("MD5", Files.readAllBytes(localFile));
    }

    private String sha256Hex(String key) throws Exception {
        try (GetObjectResponse response = minioClient.getObject(
            GetObjectArgs.builder().bucket(bucket).object(key).build()
        )) {
            return digestHex("SHA-256", response.readAllBytes());
        }
    }

    private static String sha256Hex(Path localFile) throws IOException {
        return digestHex("SHA-256", Files.readAllBytes(localFile));
    }

    private static String digestHex(String algorithm, byte[] content) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("Missing digest " + algorithm, exception);
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
