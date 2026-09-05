package com.portfolio.studio.storage;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public final class ObjectKeyValidator {

    private static final Pattern SAFE = Pattern.compile("^[a-zA-Z0-9._-]+(/[a-zA-Z0-9._-]+)*$");

    private ObjectKeyValidator() {
    }

    /**
     * Validates an object key and returns its normalized value.
     *
     * @param key the object key to validate
     * @return the normalized object key
     * @throws IllegalArgumentException if the key is invalid
     */
    public static String requireValid(String key) {
        return parse(key).orElseThrow(() -> new IllegalArgumentException("Invalid object key."));
    }

    /**
     * Parses and normalizes an object key when it meets the accepted format.
     *
     * @param key the object key to validate and normalize
     * @return the normalized key when valid, or an empty optional otherwise
     */
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
