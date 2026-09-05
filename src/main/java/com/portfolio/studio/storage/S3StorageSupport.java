package com.portfolio.studio.storage;

import java.net.URI;

import com.portfolio.studio.config.PortfolioProperties;
import org.springframework.util.StringUtils;

public final class S3StorageSupport {

    private S3StorageSupport() {
    }

    public static boolean isFullyConfigured(PortfolioProperties.Storage.S3 s3) {
        if (s3 == null || !s3.isEnabled()) {
            return false;
        }
        return StringUtils.hasText(s3.getEndpoint())
            && StringUtils.hasText(s3.getAccessKey())
            && StringUtils.hasText(s3.getSecretKey())
            && StringUtils.hasText(s3.getBucket());
    }

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
        if ("http".equalsIgnoreCase(endpoint.getScheme()) && isLoopback(endpoint.getHost())) {
            return endpoint;
        }
        throw new IllegalStateException(
            "portfolio.storage.s3.endpoint must use HTTPS, or HTTP only to 127.0.0.1/localhost."
        );
    }

    static boolean isLoopback(String host) {
        return "127.0.0.1".equals(host)
            || "localhost".equalsIgnoreCase(host)
            || "::1".equals(host);
    }
}
