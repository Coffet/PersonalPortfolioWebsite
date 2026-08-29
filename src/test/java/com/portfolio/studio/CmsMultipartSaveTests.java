package com.portfolio.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CmsMultipartSaveTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-csrf-test.db");
    private static final Path TEST_UPLOADS = TEST_ROOT.resolve("uploads");

    @Autowired
    private MockMvc mockMvc;

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
    @WithMockUser(roles = "OWNER")
    void savingProjectDraftThroughMultipartDoesNotReturnForbidden() throws Exception {
        mockMvc.perform(
                multipart("/cmsmgmnt/projects/save")
                    .param("id", "0")
                    .param("title", "Draft project")
                    .param("summary", "A quiet draft.")
                    .param("yearLabel", "2026")
                    .param("role", "Design")
                    .param("tools", "Figma")
                    .param("narrative", "Saved without publishing.")
                    .param("externalLink", "")
                    .param("linkLabel", "")
                    .param("cardGradient", "")
                    .param("cardImageMode", "cover")
                    .param("cardImageScale", "1")
                    .param("sortOrder", "1")
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/cmsmgmnt/projects"));
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-csrf-test-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }
}
