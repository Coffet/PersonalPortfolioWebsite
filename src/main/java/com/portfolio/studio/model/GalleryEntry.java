package com.portfolio.studio.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GalleryEntry {

    private long id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 120)
    private String slug;

    @Size(max = 280)
    private String introText;

    private String body;

    @Size(max = 80)
    private String category;

    private boolean published;
    private int sortOrder;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;
    private List<GalleryMedia> media = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        if (category == null || category.isBlank()) {
            return tags;
        }
        for (String part : category.split("[,/|]")) {
            String tag = part.trim();
            if (!tag.isEmpty() && !tags.contains(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<GalleryMedia> getMedia() {
        return media;
    }

    public void setMedia(List<GalleryMedia> media) {
        this.media = media;
    }

    public GalleryMedia getCoverMedia() {
        if (media == null || media.isEmpty()) {
            return null;
        }
        for (GalleryMedia item : media) {
            if (item != null && item.isCover()) {
                return item;
            }
        }
        return media.get(0);
    }

    public String getYear() {
        String source = null;
        if (publishedAt != null && !publishedAt.isBlank()) {
            source = publishedAt;
        } else if (createdAt != null && !createdAt.isBlank()) {
            source = createdAt;
        }
        if (source == null || source.length() < 4) {
            return "";
        }
        return source.substring(0, 4);
    }
}
