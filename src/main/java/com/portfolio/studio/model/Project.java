package com.portfolio.studio.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Project {

    private long id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 120)
    private String slug;

    @NotBlank
    @Size(max = 280)
    private String summary;

    @NotBlank
    @Size(max = 40)
    private String yearLabel;

    @NotBlank
    @Size(max = 180)
    private String role;

    @NotBlank
    @Size(max = 280)
    private String tools;

    @Size(max = 255)
    private String externalLink;

    @Size(max = 80)
    private String linkLabel;

    @Size(max = 255)
    private String cardImagePath;

    @Size(max = 255)
    private String fallbackImagePath;

    @Size(max = 255)
    private String cardGradient;

    @Size(max = 20)
    private String cardImageMode;

    @NotNull
    private Double cardImageScale = 1.0;

    @NotBlank
    private String narrative;

    private boolean featured;
    private boolean published;
    private int sortOrder;
    private String createdAt;
    private String updatedAt;
    private List<ProjectMedia> media = new ArrayList<>();

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getYearLabel() {
        return yearLabel;
    }

    public void setYearLabel(String yearLabel) {
        this.yearLabel = yearLabel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTools() {
        return tools;
    }

    public void setTools(String tools) {
        this.tools = tools;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(String linkLabel) {
        this.linkLabel = linkLabel;
    }

    public String getCardImagePath() {
        return cardImagePath;
    }

    public void setCardImagePath(String cardImagePath) {
        this.cardImagePath = cardImagePath;
    }

    public String getFallbackImagePath() {
        return fallbackImagePath;
    }

    public void setFallbackImagePath(String fallbackImagePath) {
        this.fallbackImagePath = fallbackImagePath;
    }

    public String getCardGradient() {
        return cardGradient;
    }

    public void setCardGradient(String cardGradient) {
        this.cardGradient = cardGradient;
    }

    public String getCardImageMode() {
        return cardImageMode;
    }

    public void setCardImageMode(String cardImageMode) {
        this.cardImageMode = cardImageMode;
    }

    public Double getCardImageScale() {
        return cardImageScale;
    }

    public void setCardImageScale(Double cardImageScale) {
        this.cardImageScale = cardImageScale;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
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

    public List<ProjectMedia> getMedia() {
        return media;
    }

    public void setMedia(List<ProjectMedia> media) {
        this.media = media;
    }
}
