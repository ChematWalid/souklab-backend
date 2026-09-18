package com.project.souklab.filestorage.s3;

import com.project.souklab.filestorage.exception.FileNotFoundStorageException;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceUnitTest {
    @Mock S3Client client;
    private StorageProperties properties;
    private S3StorageService service;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        properties.getS3().setBucket("bucket");
        properties.getS3().setAutoCreateBucket(true);
        service = new S3StorageService(properties, client,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void initializesBucketForSuccessMissingBucketAnd404() {
        service.initBucket();
        verify(client).headBucket(any(HeadBucketRequest.class));
        reset(client);
        doThrow(NoSuchBucketException.builder().build()).when(client).headBucket(any(HeadBucketRequest.class));
        service.initBucket();
        verify(client).createBucket(any(CreateBucketRequest.class));
        reset(client);
        doThrow(AwsServiceException.builder().statusCode(404).build()).when(client).headBucket(any(HeadBucketRequest.class));
        service.initBucket();
        verify(client).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void toleratesBucketCheckFailuresAndDisabledAutoCreation() {
        doThrow(AwsServiceException.builder().statusCode(500).message("unavailable").build()).when(client).headBucket(any(HeadBucketRequest.class));
        service.initBucket();
        verify(client, never()).createBucket(any(CreateBucketRequest.class));
        reset(client);
        doThrow(new IllegalStateException("unexpected")).when(client).headBucket(any(HeadBucketRequest.class));
        service.initBucket();
        reset(client);
        properties.getS3().setAutoCreateBucket(false);
        service.initBucket();
        verify(client, never()).headBucket(any(HeadBucketRequest.class));
    }

    @Test
    void wrapsStoreDeleteRetrieveAndExistsFailures() {
        doThrow(SdkClientException.create("down")).when(client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertThatThrownBy(() -> service.store(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1))
                .isInstanceOf(StorageException.class);
        doThrow(SdkClientException.create("down")).when(client).deleteObject(any(DeleteObjectRequest.class));
        assertThatThrownBy(() -> service.delete("key")).isInstanceOf(StorageException.class);
        doThrow(NoSuchKeyException.builder().build()).when(client).getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
        assertThatThrownBy(() -> service.retrieve("key")).isInstanceOf(FileNotFoundStorageException.class);
        reset(client);
        doThrow(AwsServiceException.builder().statusCode(404).build()).when(client).headObject(any(HeadObjectRequest.class));
        assertThat(service.exists("key")).isFalse();
        reset(client);
        doThrow(SdkClientException.create("down")).when(client).headObject(any(HeadObjectRequest.class));
        assertThatThrownBy(() -> service.exists("key")).isInstanceOf(StorageException.class);
    }

    @Test
    void mapsNonNotFoundAwsFailuresAndReadsObjectMetadata() throws Exception {
        GetObjectResponse response = GetObjectResponse.builder()
                .contentType("image/jpeg").contentLength(null)
                .metadata(java.util.Map.of("original-filename", "photo.jpg")).build();
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[]{1})));
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        var resource = service.retrieve("key");
        assertThat(resource.contentType()).isEqualTo("image/jpeg");
        assertThat(resource.size()).isZero();
        assertThat(resource.originalFilename()).isEqualTo("photo.jpg");
        resource.content().close();

        reset(client);
        doThrow(AwsServiceException.builder().statusCode(500).message("down").build())
                .when(client).getObject(any(GetObjectRequest.class));
        assertThatThrownBy(() -> service.retrieve("key")).isInstanceOf(StorageException.class);
        reset(client);
        doThrow(SdkClientException.create("client unavailable"))
                .when(client).getObject(any(GetObjectRequest.class));
        assertThatThrownBy(() -> service.retrieve("key"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to retrieve file from S3");
        reset(client);
        doThrow(AwsServiceException.builder().statusCode(500).message("down").build())
                .when(client).deleteObject(any(DeleteObjectRequest.class));
        assertThatThrownBy(() -> service.delete("key")).isInstanceOf(StorageException.class);
        reset(client);
        doThrow(AwsServiceException.builder().statusCode(500).message("down").build())
                .when(client).headObject(any(HeadObjectRequest.class));
        assertThatThrownBy(() -> service.exists("key")).isInstanceOf(StorageException.class);
    }

    @Test
    void rejectsNullClock() {
        assertThatThrownBy(() -> new S3StorageService(properties, client, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Clock");
    }
}
