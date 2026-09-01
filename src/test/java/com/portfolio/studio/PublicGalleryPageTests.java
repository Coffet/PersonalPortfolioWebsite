package com.portfolio.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;

import com.portfolio.studio.model.GalleryEntry;
import com.portfolio.studio.service.PortfolioService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PublicGalleryPageTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-gallery-page-test.db");
    private static final Path TEST_UPLOADS = TEST_ROOT.resolve("uploads");
    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioService portfolioService;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB.toString().replace("\\", "/"));
        registry.add("portfolio.storage.upload-root", () -> TEST_UPLOADS.toString().replace("\\", "/"));
        registry.add("portfolio.seed.enabled", () -> "false");
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
    void galleryIndexUsesFeaturedCardAndGridWithoutFilterTabs() throws Exception {
        savePublished("North Dashboard", "UI", "visualization interface with calm hierarchy");
        savePublished("Flow State", "Motion", "Motion exploration");

        mockMvc.perform(get("/gallery"))
            .andExpect(status().isOk())
            .andExpect(view().name("public/gallery"))
            .andExpect(forwardedUrl("/WEB-INF/jsp/public/gallery.jsp"))
            .andExpect(model().attributeExists("featuredEntry"))
            .andExpect(model().attribute("galleryEntries", hasSize(2)))
            .andExpect(model().attributeDoesNotExist("showGalleryFilters", "galleryCategories"));
    }

    private void savePublished(String title, String category, String intro) {
        GalleryEntry entry = new GalleryEntry();
        entry.setTitle(title);
        entry.setCategory(category);
        entry.setIntroText(intro);
        entry.setPublished(true);
        entry.setSortOrder(1);
        MockMultipartFile image = new MockMultipartFile(
            "mediaFiles",
            title.toLowerCase().replace(' ', '-') + ".png",
            "image/png",
            PIXEL_PNG
        );
        portfolioService.saveGalleryEntry(entry, new MultipartFile[]{image});
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-gallery-page-test-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }
}
