package com.portfolio.studio.model;

public class GalleryMedia {

    private long id;
    private long galleryEntryId;
    private String filePath;
    private String altText;
    private int mediaOrder;
    private boolean cover;
    private String originalFilename;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGalleryEntryId() {
        return galleryEntryId;
    }

    public void setGalleryEntryId(long galleryEntryId) {
        this.galleryEntryId = galleryEntryId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public int getMediaOrder() {
        return mediaOrder;
    }

    public void setMediaOrder(int mediaOrder) {
        this.mediaOrder = mediaOrder;
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
}
