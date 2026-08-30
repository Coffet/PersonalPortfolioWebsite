package com.portfolio.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UploadResourceCacheTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-upload-cache-test.db");
    private static final Path TEST_UPLOADS = TEST_ROOT.resolve("uploads");
    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB.toString().replace("\\", "/"));
        registry.add("portfolio.storage.upload-root", () -> TEST_UPLOADS.toString().replace("\\", "/"));
        registry.add("portfolio.seed.enabled", () -> "false");
    }

    @BeforeEach
    void writeSampleUpload() throws IOException {
        Path folder = TEST_UPLOADS.resolve("gallery");
        Files.createDirectories(folder);
        Files.write(folder.resolve("sample.png"), PIXEL_PNG);
    }

    @AfterAll
    static void cleanup() throws IOException {
        if (Files.notExists(TEST_ROOT)) {
            return;
        }

        try (var walk = Files.walk(TEST_ROOT)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // Temporary test artifacts should not fail the suite cleanup.
                    }
                });
        }
    }

    @Test
    void uploadedImagesAllowBrowserCacheInsteadOfNoStore() throws Exception {
        mockMvc.perform(get("/uploads/gallery/sample.png"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("max-age")))
            .andExpect(header().string("Cache-Control", containsString("immutable")))
            .andExpect(header().string("Cache-Control", not(containsString("no-store"))));
    }

    @Test
    void htmlPagesStillDisableStoreSoPublishedContentUpdates() throws Exception {
        mockMvc.perform(get("/gallery"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-upload-cache-test-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }
}
