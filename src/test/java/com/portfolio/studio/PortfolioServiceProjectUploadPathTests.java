package com.portfolio.studio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.portfolio.studio.model.GalleryEntry;
import com.portfolio.studio.model.Project;
import com.portfolio.studio.service.PortfolioService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PortfolioServiceProjectUploadPathTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-test.db");
    private static final Path TEST_UPLOADS = TEST_ROOT.resolve("uploads");

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
    void storesProjectCardImagesInProjectSpecificFolders() throws IOException {
        Files.createDirectories(TEST_UPLOADS);

        Project project = new Project();
        project.setTitle("Project Upload Test");
        project.setSlug("project-upload-test");
        project.setSummary("Checks that project uploads use the PJKT folder convention.");
        project.setYearLabel("2026");
        project.setRole("Verification");
        project.setTools("Spring Boot, SQLite");
        project.setNarrative("A targeted test that verifies project card uploads are isolated per project.");
        project.setCardImageMode("cover");
        project.setCardImageScale(1.0);
        project.setFeatured(false);
        project.setPublished(false);
        project.setSortOrder(99);

        MockMultipartFile cardImageFile = new MockMultipartFile(
            "cardImageFile",
            "project-test.png",
            "image/png",
            createPngBytes()
        );

        long projectId = portfolioService.saveProject(project, cardImageFile, null);
        Project savedProject = portfolioService.findProjectById(projectId).orElseThrow();

        assertThat(savedProject.getCardImagePath())
            .matches("/uploads/projects/PJKT_" + projectId + "_img/[^/]+\\.png");

        String relativeUploadPath = savedProject.getCardImagePath().substring("/uploads/".length());
        Path storedFile = TEST_UPLOADS.resolve(Path.of(relativeUploadPath));
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(storedFile.getParent().getFileName().toString()).isEqualTo("PJKT_" + projectId + "_img");
    }

    @Test
    void storesAndAppendsMultipleGalleryImages() throws IOException {
        Files.createDirectories(TEST_UPLOADS);

        GalleryEntry entry = new GalleryEntry();
        entry.setTitle("Multi image gallery");
        entry.setPublished(false);
        entry.setSortOrder(1);

        long entryId = portfolioService.saveGalleryEntry(
            entry,
            new MockMultipartFile[] {
                pngFile("mediaFiles", "one.png"),
                pngFile("mediaFiles", "two.png")
            }
        );

        GalleryEntry saved = portfolioService.findGalleryEntryById(entryId).orElseThrow();
        assertThat(saved.getMedia()).hasSize(2);
        assertThat(saved.getMedia().get(0).isCover()).isTrue();
        assertThat(saved.getMedia().get(1).isCover()).isFalse();

        saved.setTitle("Multi image gallery");
        portfolioService.saveGalleryEntry(saved, new MockMultipartFile[] { pngFile("mediaFiles", "three.png") });

        GalleryEntry appended = portfolioService.findGalleryEntryById(entryId).orElseThrow();
        assertThat(appended.getMedia()).hasSize(3);
        assertThat(appended.getMedia().get(0).getOriginalFilename()).isEqualTo("one.png");
        assertThat(appended.getMedia().get(2).getOriginalFilename()).isEqualTo("three.png");
    }

    private static MockMultipartFile pngFile(String fieldName, String filename) throws IOException {
        return new MockMultipartFile(fieldName, filename, "image/png", createPngBytes());
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-upload-test-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }

    private static byte[] createPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x101820);
        image.setRGB(1, 0, 0xB8E1FF);
        image.setRGB(0, 1, 0xC9F0FF);
        image.setRGB(1, 1, 0x1F2A44);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
