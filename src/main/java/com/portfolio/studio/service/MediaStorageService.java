package com.portfolio.studio.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.portfolio.studio.config.PortfolioProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/png",
        "image/jpeg",
        "image/webp",
        "image/gif"
    );

    private final Path uploadRoot;

    public MediaStorageService(PortfolioProperties portfolioProperties) {
        this.uploadRoot = Paths.get(portfolioProperties.getStorage().getUploadRoot()).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose an image to upload.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PNG, JPG, JPEG, WEBP, and GIF images are allowed.");
        }

        validateImage(file);

        try {
            Path targetFolder = uploadRoot.resolve(folder).normalize();
            Files.createDirectories(targetFolder);
            String storedFilename = UUID.randomUUID() + "." + extension;
            Path storedPath = targetFolder.resolve(storedFilename).normalize();
            if (!storedPath.startsWith(targetFolder)) {
                throw new IllegalArgumentException("Invalid upload target.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicPath = "/uploads/" + folder + "/" + storedFilename;
            return new StoredFile(publicPath, originalFilename);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store image file.", exception);
        }
    }

    public void deleteIfPresent(String publicPath) {
        if (!StringUtils.hasText(publicPath) || !publicPath.startsWith("/uploads/")) {
            return;
        }

        try {
            String relative = publicPath.substring("/uploads/".length()).replace("/", java.io.File.separator);
            Path filePath = uploadRoot.resolve(relative).normalize();
            if (filePath.startsWith(uploadRoot)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
            // A missing file should not block content deletion.
        }
    }

    public List<StoredFile> storeAll(MultipartFile[] files, String folder) {
        if (files == null || files.length == 0) {
            return List.of();
        }

        return java.util.Arrays.stream(files)
            .filter(file -> file != null && !file.isEmpty())
            .map(file -> store(file, folder))
            .toList();
    }

    private void validateImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Uploaded file is not a readable image.");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Uploaded image could not be validated.", exception);
        }
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(String publicPath, String originalFilename) {
    }
}
