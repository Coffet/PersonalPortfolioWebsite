package com.portfolio.studio.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectKeyValidatorTests {

    @Test
    void acceptsGalleryAndProjectKeys() {
        assertThat(ObjectKeyValidator.parse("gallery/a1b2c3d4-e5f6-7890-abcd-ef1234567890.png"))
            .contains("gallery/a1b2c3d4-e5f6-7890-abcd-ef1234567890.png");
        assertThat(ObjectKeyValidator.parse("projects/PJKT_12_img/cover.webp"))
            .contains("projects/PJKT_12_img/cover.webp");
    }

    @Test
    void rejectsTraversalAndAbsolutePaths() {
        assertThat(ObjectKeyValidator.parse("../secret.png")).isEmpty();
        assertThat(ObjectKeyValidator.parse("/uploads/gallery/x.png")).isEmpty();
        assertThat(ObjectKeyValidator.parse("gallery//x.png")).isEmpty();
        assertThat(ObjectKeyValidator.parse("gallery/../x.png")).isEmpty();
        assertThat(ObjectKeyValidator.parse("")).isEmpty();
    }

    @Test
    void requireValidThrowsOnBadKey() {
        assertThatThrownBy(() -> ObjectKeyValidator.requireValid("../x.png"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid object key");
    }
}
