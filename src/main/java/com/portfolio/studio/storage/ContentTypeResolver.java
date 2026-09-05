package com.portfolio.studio.storage;

import java.util.Locale;

import org.springframework.util.StringUtils;

public final class ContentTypeResolver {

    private ContentTypeResolver() {
    }

    public static String fromKey(String key) {
        if (!StringUtils.hasText(key)) {
            return "application/octet-stream";
        }

        int lastDot = key.lastIndexOf('.');
        if (lastDot < 0 || lastDot == key.length() - 1) {
            return "application/octet-stream";
        }

        return switch (key.substring(lastDot + 1).toLowerCase(Locale.ROOT)) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
