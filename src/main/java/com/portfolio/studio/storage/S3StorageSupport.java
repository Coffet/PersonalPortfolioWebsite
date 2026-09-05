package com.portfolio.studio.storage;

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
}
