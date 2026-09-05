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

    public LocalDiskObjectStore(Path uploadRoot) {
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    @Override
    public void put(String key, InputStream content, long contentLength, String contentType) throws IOException {
        Path target = resolveExistingOrCreate(key);
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Optional<StoredObject> get(String key) throws IOException {
        Path filePath = resolveIfPresent(key);
        if (filePath == null || !Files.isRegularFile(filePath)) {
            return Optional.empty();
        }

        InputStream content = Files.newInputStream(filePath);
        return Optional.of(new StoredObject(content, Files.size(filePath), ContentTypeResolver.fromKey(key)));
    }

    @Override
    public boolean exists(String key) {
        Path filePath = resolveIfPresent(key);
        return filePath != null && Files.isRegularFile(filePath);
    }

    @Override
    public void deleteIfPresent(String key) {
        Path filePath = resolveIfPresent(key);
        if (filePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // A missing file should not block content deletion.
        }
    }

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

    private Path resolveExistingOrCreate(String key) {
        String valid = ObjectKeyValidator.requireValid(key);
        Path target = uploadRoot.resolve(valid).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid object key.");
        }
        return target;
    }

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
