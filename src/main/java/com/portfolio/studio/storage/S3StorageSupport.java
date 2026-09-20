package com.portfolio.studio.storage;

import java.net.URI;

import com.portfolio.studio.config.PortfolioProperties;
import org.springframework.util.StringUtils;

public final class S3StorageSupport {

    private S3StorageSupport() {
    }

    /**
     * Determines whether S3 storage is enabled and has all required configuration values.
     *
     * @param s3 the S3 storage configuration
     * @return {@code true} if the configuration is enabled and contains an endpoint, access key,
     *         secret key, and bucket, {@code false} otherwise
     */
    public static boolean isFullyConfigured(PortfolioProperties.Storage.S3 s3) {
        if (s3 == null || !s3.isEnabled()) {
            return false;
        }
        return StringUtils.hasText(s3.getEndpoint())
            && StringUtils.hasText(s3.getAccessKey())
            && StringUtils.hasText(s3.getSecretKey())
            && StringUtils.hasText(s3.getBucket());
    }

    /**
     * Validates and normalizes an S3 endpoint for use with configured credentials.
     *
     * @param rawEndpoint the endpoint value to validate
     * @return the trimmed HTTPS endpoint
     * @throws IllegalStateException if the endpoint is blank, lacks a scheme or host,
     *                               or does not use HTTPS
     */
    public static URI requireAllowedEndpoint(String rawEndpoint) {
        if (!StringUtils.hasText(rawEndpoint)) {
            throw new IllegalStateException(
                "portfolio.storage.s3.endpoint must be an absolute URL."
            );
        }
        URI endpoint = URI.create(rawEndpoint.trim());
        if (endpoint.getScheme() == null || endpoint.getHost() == null) {
            throw new IllegalStateException(
                "portfolio.storage.s3.endpoint must be an absolute URL."
            );
        }
        if ("https".equalsIgnoreCase(endpoint.getScheme())) {
            return endpoint;
        }
        throw new IllegalStateException(
            "portfolio.storage.s3.endpoint must use HTTPS when credentials are configured."
        );
    }
}
