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

    /**
     * Creates a controller backed by local storage with optional Minio storage fallback.
     *
     * @param localDiskObjectStore the local object store used to retrieve uploads
     * @param minioObjectStoreProvider the provider for an optional Minio object store
     */
    public UploadController(
        LocalDiskObjectStore localDiskObjectStore,
        ObjectProvider<MinioObjectStore> minioObjectStoreProvider
    ) {
        this.localDiskObjectStore = localDiskObjectStore;
        this.minioObjectStore = minioObjectStoreProvider.getIfAvailable();
    }

    /**
     * Serves a validated uploaded object from the configured object stores.
     *
     * @param request the HTTP request containing the uploaded object's path
     * @return the object stream with its content metadata, or a not-found response when the path or object is unavailable
     * @throws IOException if the object stream cannot be accessed
     */
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

    /**
     * Extracts and validates the object key from an upload request URI.
     *
     * @param request the HTTP request containing the upload URI
     * @return the validated object key, or an empty result when the URI does not match the upload path or the key is invalid
     */
    private static Optional<String> extractKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = request.getContextPath() + "/uploads/";
        if (uri == null || !uri.startsWith(prefix)) {
            return Optional.empty();
        }
        String decoded = URLDecoder.decode(uri.substring(prefix.length()), StandardCharsets.UTF_8);
        return ObjectKeyValidator.parse(decoded);
    }

    /**
     * Parses a content type and falls back to {@code application/octet-stream} when it is blank or invalid.
     *
     * @param contentType the content type to parse
     * @return the parsed media type or {@code application/octet-stream} when parsing fails
     */
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
