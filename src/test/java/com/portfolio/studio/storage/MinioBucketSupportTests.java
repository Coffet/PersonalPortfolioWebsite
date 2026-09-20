package com.portfolio.studio.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MinioBucketSupportTests {

    @Test
    void returnsTrueWhenBucketExists() throws Exception {
        MinioClient client = mock(MinioClient.class);
        when(client.bucketExists(any())).thenReturn(true);

        assertThat(MinioBucketSupport.ensureBucket(client, "portfolio-uploads")).isTrue();
        verify(client, never()).makeBucket(any());
    }

    @Test
    void returnsFalseForBlankBucketWithoutCallingMinio() {
        MinioClient client = mock(MinioClient.class);

        assertThat(MinioBucketSupport.ensureBucket(client, "  ")).isFalse();
        assertThat(MinioBucketSupport.ensureBucket(client, null)).isFalse();
        verifyNoInteractions(client);
    }

    @Test
    void returnsFalseWhenMinioIsUnreachable() throws Exception {
        MinioClient client = mock(MinioClient.class);
        when(client.bucketExists(any())).thenThrow(new RuntimeException("connection refused"));

        assertThat(MinioBucketSupport.ensureBucket(client, "portfolio-uploads")).isFalse();
        verify(client, never()).makeBucket(any());
    }

    @Test
    void createsMissingBucket() throws Exception {
        MinioClient client = mock(MinioClient.class);
        when(client.bucketExists(any())).thenReturn(false);

        assertThat(MinioBucketSupport.ensureBucket(client, "portfolio-uploads")).isTrue();
        verify(client).makeBucket(any());
    }
}
