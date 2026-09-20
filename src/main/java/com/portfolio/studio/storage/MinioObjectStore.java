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

    /**
     * Creates an object store backed by the specified MinIO bucket.
     *
     * @param minioClient the client used to access MinIO
     * @param bucket      the bucket containing stored objects
     */
    public MinioObjectStore(MinioClient minioClient, String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    /**
     * Stores content under the specified object key.
     *
     * @param key           the object key
     * @param content       the content stream
     * @param contentLength the content length in bytes
     * @param contentType   the content type, or {@code null} or blank to infer it from the key
     * @throws IOException if the object cannot be stored
     */
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

    /**
     * Retrieves an object and its metadata from the configured object store.
     *
     * @param key the object key
     * @return the stored object, or an empty optional if the object is missing
     * @throws IOException if the object cannot be read
     */
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

    /**
     * Checks whether an object exists in the configured bucket.
     *
     * @param key the object key to check
     * @return true if the object exists, false if it is missing
     * @throws IOException if the key is invalid or the object status cannot be checked
     */
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

    /**
     * Deletes the object identified by the key when it exists.
     *
     * @param key the object key to delete
     * @throws IOException if deleting the object fails
     */
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

    /**
     * Determines whether a local file has the same content as the stored object.
     *
     * @param key       the object key
     * @param localFile the local file to compare
     * @return {@code true} if both objects exist and have matching size and SHA-256 content,
     *         {@code false} otherwise
     * @throws IOException if the object or local file cannot be compared
     */
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

    /**
     * Computes the SHA-256 digest of the supplied content as a lowercase hexadecimal string.
     *
     * @param content the content to digest
     * @return the lowercase hexadecimal SHA-256 digest
     * @throws IOException if the content cannot be read or SHA-256 is unavailable
     */
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

    /**
     * Determines whether an error indicates that an object is missing.
     *
     * @param exception the MinIO error to inspect
     * @return {@code true} if the error code indicates a missing object, {@code false} otherwise
     */
    private static boolean isMissingObject(ErrorResponseException exception) {
        if (exception.errorResponse() == null) {
            return false;
        }
        String code = exception.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchObject".equals(code);
    }

    /**
     * Preserves an existing {@link IOException} or wraps another exception with the specified message.
     *
     * @param message   the message for the wrapped exception
     * @param exception the exception to preserve or wrap
     * @return the original {@link IOException}, or a new {@link IOException}
     */
    private static IOException wrap(String message, Exception exception) {
        if (exception instanceof IOException ioException) {
            return ioException;
        }
        return new IOException(message, exception);
    }
}
