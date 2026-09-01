package com.portfolio.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.portfolio.studio.model.BlogPost;
import com.portfolio.studio.model.GalleryEntry;
import com.portfolio.studio.model.GalleryMedia;
import com.portfolio.studio.service.PortfolioService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PortfolioServiceSlugTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-slug-test.db");
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
    void generatesUniqueSlugFromTitleWhenSlugIsBlank() {
        BlogPost first = newBlog("Quiet notes from the desk", "First excerpt.", "First body.");
        long firstId = portfolioService.saveBlogPost(first, null);
        BlogPost savedFirst = portfolioService.findBlogPostById(firstId).orElseThrow();

        assertThat(savedFirst.getSlug()).isEqualTo("quiet-notes-from-the-desk");
        assertThat(portfolioService.findPublishedBlogPostById(firstId)).isEmpty();

        savedFirst.setPublished(true);
        portfolioService.saveBlogPost(savedFirst, null);
        assertThat(portfolioService.findPublishedBlogPostById(firstId)).isPresent();

        BlogPost second = newBlog("Quiet notes from the desk", "Second excerpt.", "Second body.");
        long secondId = portfolioService.saveBlogPost(second, null);
        BlogPost savedSecond = portfolioService.findBlogPostById(secondId).orElseThrow();

        assertThat(savedSecond.getSlug()).isEqualTo("quiet-notes-from-the-desk-2");
    }

    @Test
    void savesGalleryEntryWithTitleOnly() {
        GalleryEntry entry = new GalleryEntry();
        entry.setTitle("Title only");
        entry.setPublished(false);
        entry.setSortOrder(1);

        long id = portfolioService.saveGalleryEntry(entry, null);
        GalleryEntry saved = portfolioService.findGalleryEntryById(id).orElseThrow();

        assertThat(saved.getTitle()).isEqualTo("Title only");
        assertThat(saved.getIntroText()).isEmpty();
        assertThat(saved.getBody()).isEmpty();
        assertThat(saved.getSlug()).isEqualTo("title-only");
    }

    @Test
    void pickRandomGalleryEntryReturnsEmptyWhenThereAreNoEntries() {
        assertThat(portfolioService.pickRandomGalleryEntry(List.of())).isEmpty();
        assertThat(portfolioService.pickRandomGalleryEntry(null)).isEmpty();
    }

    @Test
    void pickRandomGalleryEntryCanFeatureTheFirstUploadedEntry() {
        GalleryEntry first = new GalleryEntry();
        first.setTitle("Alone on the wall");
        GalleryMedia media = new GalleryMedia();
        media.setFilePath("/uploads/cms/alone.png");
        first.setMedia(List.of(media));

        assertThat(portfolioService.pickRandomGalleryEntry(List.of(first)).orElseThrow().getTitle())
            .isEqualTo("Alone on the wall");
    }

    @Test
    void pickRandomGalleryEntryPrefersEntriesWithMedia() {
        GalleryEntry withoutMedia = new GalleryEntry();
        withoutMedia.setTitle("Notes only");

        GalleryEntry withMedia = new GalleryEntry();
        withMedia.setTitle("Printed study");
        GalleryMedia media = new GalleryMedia();
        media.setFilePath("/uploads/cms/printed-study.png");
        withMedia.setMedia(List.of(media));

        assertThat(portfolioService.pickRandomGalleryEntry(List.of(withoutMedia, withMedia)).orElseThrow().getTitle())
            .isEqualTo("Printed study");
    }

    @Test
    void pickRandomGalleryPictureChoosesFromAllUploadedImages() {
        GalleryEntry study = new GalleryEntry();
        study.setId(3);
        GalleryMedia first = new GalleryMedia();
        first.setId(11);
        first.setGalleryEntryId(3);
        first.setFilePath("/uploads/cms/one.png");
        GalleryMedia second = new GalleryMedia();
        second.setId(12);
        second.setGalleryEntryId(3);
        second.setFilePath("/uploads/cms/two.png");
        study.setMedia(List.of(first, second));

        GalleryMedia picked = portfolioService.pickRandomGalleryPicture(List.of(study)).orElseThrow();
        assertThat(picked.getFilePath()).isIn("/uploads/cms/one.png", "/uploads/cms/two.png");
    }

    @Test
    void recentActivityShowsDraftPublishedAndRemoved() {
        GalleryEntry entry = new GalleryEntry();
        entry.setTitle("Activity status check");
        entry.setPublished(false);
        entry.setSortOrder(9);

        long id = portfolioService.saveGalleryEntry(entry, null);
        Map<String, Object> draft = activityFor("Activity status check");
        assertThat(draft.get("actionLabel")).isEqualTo("Saved");
        assertThat(draft.get("statusLabel")).isEqualTo("Draft");
        assertThat(draft.get("kindLabel")).isEqualTo("Gallery");

        GalleryEntry saved = portfolioService.findGalleryEntryById(id).orElseThrow();
        saved.setPublished(true);
        portfolioService.saveGalleryEntry(saved, null);
        Map<String, Object> published = activityFor("Activity status check");
        assertThat(published.get("actionLabel")).isEqualTo("Saved");
        assertThat(published.get("statusLabel")).isEqualTo("Published");

        portfolioService.deleteGalleryEntry(id);
        Map<String, Object> removed = activityFor("Activity status check");
        assertThat(removed.get("actionLabel")).isEqualTo("Removed");
        assertThat(removed.get("statusLabel")).isEqualTo("Removed");
    }

    private Map<String, Object> activityFor(String title) {
        return portfolioService.listRecentAuditEntries().stream()
            .filter(item -> title.equals(item.get("title")))
            .findFirst()
            .orElseThrow();
    }

    private static BlogPost newBlog(String title, String excerpt, String body) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setExcerpt(excerpt);
        post.setBody(body);
        post.setPublished(false);
        post.setSortOrder(1);
        return post;
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-slug-test-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }
}
