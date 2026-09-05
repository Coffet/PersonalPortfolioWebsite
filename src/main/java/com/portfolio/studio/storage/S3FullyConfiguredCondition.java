package com.portfolio.studio.storage;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class S3FullyConfiguredCondition implements Condition {

    /**
     * Determines whether the S3 configuration is fully enabled and populated.
     *
     * @return {@code true} if S3 is enabled and all required properties contain text,
     *         {@code false} otherwise
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        if (!environment.getProperty("portfolio.storage.s3.enabled", Boolean.class, false)) {
            return false;
        }
        return StringUtils.hasText(environment.getProperty("portfolio.storage.s3.endpoint"))
            && StringUtils.hasText(environment.getProperty("portfolio.storage.s3.access-key"))
            && StringUtils.hasText(environment.getProperty("portfolio.storage.s3.secret-key"))
            && StringUtils.hasText(environment.getProperty("portfolio.storage.s3.bucket"));
    }
}
