package com.portfolio.studio.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GalleryEntryTest {

    @Test
    void coverMediaPrefersTheCoverFlagThenFallsBackToTheFirstImage() {
        GalleryEntry entry = new GalleryEntry();
        GalleryMedia first = new GalleryMedia();
        first.setId(1);
        first.setFilePath("/uploads/cms/first.png");
        GalleryMedia cover = new GalleryMedia();
        cover.setId(2);
        cover.setFilePath("/uploads/cms/cover.png");
        cover.setCover(true);

        assertThat(entry.getCoverMedia()).isNull();

        entry.setMedia(List.of(first));
        assertThat(entry.getCoverMedia()).isSameAs(first);

        entry.setMedia(List.of(first, cover));
        assertThat(entry.getCoverMedia()).isSameAs(cover);
    }

    @Test
    void yearReadsPublishedDateThenCreatedDate() {
        GalleryEntry entry = new GalleryEntry();
        assertThat(entry.getYear()).isEmpty();

        entry.setCreatedAt("2023-04-01 10:00:00");
        assertThat(entry.getYear()).isEqualTo("2023");

        entry.setPublishedAt("2024-11-02 09:00:00");
        assertThat(entry.getYear()).isEqualTo("2024");
    }

    @Test
    void tagsSplitFromTheCategoryField() {
        GalleryEntry entry = new GalleryEntry();
        assertThat(entry.getTags()).isEmpty();

        entry.setCategory("UI, Motion, study");
        assertThat(entry.getTags()).containsExactly("UI", "Motion", "study");
    }
}
