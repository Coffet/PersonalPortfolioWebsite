package com.portfolio.studio.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import com.portfolio.studio.storage.LocalDiskObjectStore;
import com.portfolio.studio.storage.MinioObjectStore;
import com.portfolio.studio.storage.ObjectKeyValidator;
import com.portfolio.studio.storage.ObjectStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UploadController {

    private final LocalDiskObjectStore localDiskObjectStore;
    private final MinioObjectStore minioObjectStore;

    public UploadController(
        LocalDiskObjectStore localDiskObjectStore,
        ObjectProvider<MinioObjectStore> minioObjectStoreProvider
    ) {
        this.localDiskObjectStore = localDiskObjectStore;
        this.minioObjectStore = minioObjectStoreProvider.getIfAvailable();
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<InputStreamResource> getUpload(HttpServletRequest request) throws IOException {
        Optional<String> key = extractKey(request);
        if (key.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<ObjectStore.StoredObject> stored = localDiskObjectStore.get(key.get());
        if (stored.isEmpty() && minioObjectStore != null) {
            stored = minioObjectStore.get(key.get());
        }
        if (stored.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ObjectStore.StoredObject object = stored.get();
        MediaType mediaType = parseMediaType(object.contentType());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .contentType(mediaType)
            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        if (object.contentLength() >= 0) {
            builder.contentLength(object.contentLength());
        }
        return builder.body(new InputStreamResource(object.content()));
    }

    private static Optional<String> extractKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = request.getContextPath() + "/uploads/";
        if (uri == null || !uri.startsWith(prefix)) {
            return Optional.empty();
        }
        String decoded = URLDecoder.decode(uri.substring(prefix.length()), StandardCharsets.UTF_8);
        return ObjectKeyValidator.parse(decoded);
    }

    private static MediaType parseMediaType(String contentType) {
        try {
            if (contentType != null && !contentType.isBlank()) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (Exception ignored) {
            // Fall through to a safe default.
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
