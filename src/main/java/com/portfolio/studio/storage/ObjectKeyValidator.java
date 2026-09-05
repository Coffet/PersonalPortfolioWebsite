package com.portfolio.studio.storage;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public final class ObjectKeyValidator {

    private static final Pattern SAFE = Pattern.compile("^[a-zA-Z0-9._-]+(/[a-zA-Z0-9._-]+)*$");

    private ObjectKeyValidator() {
    }

    public static String requireValid(String key) {
        return parse(key).orElseThrow(() -> new IllegalArgumentException("Invalid object key."));
    }

    public static Optional<String> parse(String key) {
        if (!StringUtils.hasText(key)) {
            return Optional.empty();
        }

        String normalized = key.replace('\\', '/').trim();
        if (normalized.startsWith("/") || normalized.endsWith("/")
            || normalized.contains("..") || normalized.contains("//")
            || !SAFE.matcher(normalized).matches()) {
            return Optional.empty();
        }

        return Optional.of(normalized);
    }
}
