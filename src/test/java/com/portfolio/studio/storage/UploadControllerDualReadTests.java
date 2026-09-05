package com.portfolio.studio.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

import com.portfolio.studio.controller.UploadController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UploadControllerDualReadTests {

    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @TempDir
    Path tempDir;

    private LocalDiskObjectStore localDiskObjectStore;
    private MinioObjectStore minioObjectStore;
    private UploadController controller;

    @BeforeEach
    void setUp() {
        localDiskObjectStore = new LocalDiskObjectStore(tempDir);
        minioObjectStore = mock(MinioObjectStore.class);
        controller = new UploadController(localDiskObjectStore, provider(minioObjectStore));
    }

    @Test
    void servesDiskFirst() throws IOException {
        Path filePath = tempDir.resolve("gallery/sample.png");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, PIXEL_PNG);

        ResponseEntity<?> response = controller.getUpload(request("/uploads/gallery/sample.png"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getCacheControl()).contains("max-age");
        assertThat(response.getHeaders().getCacheControl()).contains("immutable");
    }

    @Test
    void fallsBackToMinioWhenDiskMisses() throws IOException {
        when(minioObjectStore.get("gallery/remote.png")).thenReturn(Optional.of(
            new ObjectStore.StoredObject(new ByteArrayInputStream(PIXEL_PNG), PIXEL_PNG.length, "image/png")
        ));

        ResponseEntity<?> response = controller.getUpload(request("/uploads/gallery/remote.png"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
    }

    @Test
    void returnsNotFoundWhenNeitherStoreHasTheKey() throws IOException {
        when(minioObjectStore.get("gallery/missing.png")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getUpload(request("/uploads/gallery/missing.png"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void rejectsTraversalAsNotFound() throws IOException {
        ResponseEntity<?> response = controller.getUpload(request("/uploads/../secret.png"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private static MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        request.setContextPath("");
        return request;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<MinioObjectStore> provider(MinioObjectStore store) {
        ObjectProvider<MinioObjectStore> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(store);
        return objectProvider;
    }
}
