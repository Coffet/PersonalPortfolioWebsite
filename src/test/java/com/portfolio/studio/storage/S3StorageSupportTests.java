package com.portfolio.studio.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3StorageSupportTests {

    @Test
    void acceptsHttps() {
        assertThat(S3StorageSupport.requireAllowedEndpoint("https://objects.example:9000").getHost())
            .isEqualTo("objects.example");
        assertThat(S3StorageSupport.requireAllowedEndpoint("https://127.0.0.1:9000").getScheme())
            .isEqualTo("https");
    }

    @Test
    void rejectsHttpIncludingLoopbackBeforeAnyMinioCall() {
        assertThatThrownBy(() -> S3StorageSupport.requireAllowedEndpoint("http://127.0.0.1:9000"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("HTTPS");
        assertThatThrownBy(() -> S3StorageSupport.requireAllowedEndpoint("http://localhost:9000"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("HTTPS");
        assertThatThrownBy(() -> S3StorageSupport.requireAllowedEndpoint("http://192.168.1.10:9000"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("HTTPS");
    }
}
