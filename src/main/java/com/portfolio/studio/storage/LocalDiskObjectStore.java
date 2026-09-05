package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LocalDiskObjectStore implements ObjectStore {

    private final Path uploadRoot;

    /**
     * Creates a filesystem-backed object store rooted at the specified directory.
     *
     * @param uploadRoot the directory used to store objects
     */
    public LocalDiskObjectStore(Path uploadRoot) {
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
    }

    /**
     * Gets the configured upload root directory.
     *
     * @return the absolute, normalized upload root path
     */
    public Path getUploadRoot() {
        return uploadRoot;
    }

    /**
     * Stores the object content under the specified key, replacing any existing object.
     *
     * @param key           the object key
     * @param content       the object content
     * @param contentLength the content length
     * @param contentType   the content type
     * @throws IOException if the object cannot be stored
     */
    @Override
    public void put(String key, InputStream content, long contentLength, String contentType) throws IOException {
        Path target = resolveExistingOrCreate(key);
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Retrieves an object stored under the specified key.
     *
     * @param key the object key
     * @return the stored object, or an empty result if the key is invalid or no regular file exists
     * @throws IOException if the object cannot be opened or its size cannot be determined
     */
    @Override
    public Optional<StoredObject> get(String key) throws IOException {
        Path filePath = resolveIfPresent(key);
        if (filePath == null || !Files.isRegularFile(filePath)) {
            return Optional.empty();
        }

        InputStream content = Files.newInputStream(filePath);
        return Optional.of(new StoredObject(content, Files.size(filePath), ContentTypeResolver.fromKey(key)));
    }

    /**
     * Determines whether an object exists for the specified key.
     *
     * @param key the object key
     * @return {@code true} if the key resolves to a regular file, {@code false} otherwise
     */
    @Override
    public boolean exists(String key) {
        Path filePath = resolveIfPresent(key);
        return filePath != null && Files.isRegularFile(filePath);
    }

    /**
     * Deletes the object identified by the key when it exists.
     *
     * @param key the object key to delete
     * @throws IOException if the file cannot be deleted
     */
    @Override
    public void deleteIfPresent(String key) throws IOException {
        Path filePath = resolveIfPresent(key);
        if (filePath == null) {
            return;
        }
        Files.deleteIfExists(filePath);
    }

    /**
     * Lists valid object keys for regular files stored under the upload root.
     *
     * @return the valid object keys, or an empty list when the upload root is not a directory
     * @throws IOException if the filesystem cannot be traversed
     */
    public List<String> listKeys() throws IOException {
        if (!Files.isDirectory(uploadRoot)) {
            return List.of();
        }

        List<String> keys = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(uploadRoot)) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                String relative = uploadRoot.relativize(path).toString().replace('\\', '/');
                ObjectKeyValidator.parse(relative).ifPresent(keys::add);
            });
        }
        return keys;
    }

    /**
     * Resolves a valid object key to a path within the upload root.
     *
     * @param key the object key to resolve
     * @return the normalized path for the object key
     * @throws IllegalArgumentException if the key is invalid or resolves outside the upload root
     */
    private Path resolveExistingOrCreate(String key) {
        String valid = ObjectKeyValidator.requireValid(key);
        Path target = uploadRoot.resolve(valid).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid object key.");
        }
        return target;
    }

    /**
     * Resolves a valid object key within the upload root.
     *
     * @param key the object key to resolve
     * @return the normalized path for a valid key within the upload root, or {@code null} otherwise
     */
    private Path resolveIfPresent(String key) {
        return ObjectKeyValidator.parse(key).map(valid -> {
            Path target = uploadRoot.resolve(valid).normalize();
            if (!target.startsWith(uploadRoot)) {
                return null;
            }
            return target;
        }).orElse(null);
    }
}
