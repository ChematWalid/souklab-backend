package com.project.souklab.filestorage.s3;

import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileNotFoundStorageException;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * S3-compatible implementation of {@link StorageService}.
 * Works seamlessly against local MinIO in development and AWS S3 or Cloudflare R2 in production.
 *
 * <p><strong>STREAMING UPLOAD STRATEGY:</strong>
 * Uploads stream directly from the validated {@link InputStream} using the known, pre-validated size
 * via {@link RequestBody#fromInputStream(InputStream, long)}. There is no full in-memory buffering,
 * so heap consumption remains strictly bounded to whatever internal chunking buffer the AWS SDK uses,
 * rather than scaling with the full file size. This approach deliberately trades away the AWS SDK's
 * automatic retry capability on partial upload failures (as a raw InputStream cannot be rewound for
 * retransmission without buffering), a conscious architectural choice given this module's strict size caps
 * and memory protection invariants.
 *
 * <p><strong>CRITICAL STREAM INVARIANT:</strong>
 * Per the file-storage security architecture, {@link #store(InputStream, String, String, long)}
 * MUST consume the stream passed to it directly (which was wrapped in {@link SizeLimitingInputStream}
 * during validation) and NEVER re-wrap, substitute, or bypass it with a raw stream. The hard size cap
 * and memory defenses strictly depend on this invariant being respected.
 */
@Slf4j
public class S3StorageService implements StorageService {

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final Clock clock;

    public S3StorageService(StorageProperties properties, S3Client s3Client) {
        this(properties, s3Client, Clock.systemUTC());
    }

    public S3StorageService(StorageProperties properties, S3Client s3Client, Clock clock) {
        this.properties = properties;
        this.s3Client = s3Client;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Initializes the target S3 bucket on startup if auto-creation is enabled.
     * Verifies existence via {@code headBucket} and creates the bucket if not found.
     */
    @PostConstruct
    public void initBucket() {
        if (Boolean.TRUE.equals(properties.getS3().getAutoCreateBucket())) {
            String bucket = properties.getS3().getBucket();
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
                log.info("S3 storage bucket '{}' verified.", bucket);
            } catch (NoSuchBucketException e) {
                log.info("S3 storage bucket '{}' does not exist. Creating...", bucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("S3 storage bucket '{}' created successfully.", bucket);
            } catch (AwsServiceException e) {
                if (e.statusCode() == 404) {
                    log.info("S3 storage bucket '{}' returned 404. Creating...", bucket);
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                    log.info("S3 storage bucket '{}' created successfully.", bucket);
                } else {
                    log.warn("Could not check/create S3 storage bucket '{}': {}", bucket, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Could not verify S3 storage bucket '{}' on startup: {}", bucket, e.getMessage());
            }
        }
    }

    /**
     * Stores a file in S3 by streaming directly from the validated {@link InputStream}.
     * Enforces the critical stream invariant by consuming the wrapped stream directly,
     * avoiding full in-memory buffering so memory usage remains bounded to SDK internal chunking.
     * Note: Non-buffered streaming deliberately trades away the SDK's automatic retry on partial failure,
     * as unbuffered streams cannot be rewound.
     *
     * @param content stream to store
     * @param originalFilename original user filename hint
     * @param contentType MIME type of the file
     * @param size size in bytes
     * @return StorageResult containing generated storage key and metadata
     */
    @Override
    public StorageResult store(InputStream content, String originalFilename, String contentType, long size) {
        String key = generateKey(originalFilename);
        String bucket = properties.getS3().getBucket();

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .metadata(Map.of("original-filename", originalFilename != null ? originalFilename : ""))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(content, size));
            log.debug("Stored file in S3 via direct stream [bucket={}, key={}, size={} bytes]", bucket, key, size);

            return new StorageResult(key, originalFilename, contentType, size, Instant.now(clock));
        } catch (FileTooLargeException e) {
            throw e;
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Failed to store file in S3 [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to store file in S3: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a stored file from S3 as a {@link StorageResource}.
     *
     * @param key unique storage identifier
     * @return StorageResource containing content stream and metadata
     * @throws FileNotFoundStorageException if key does not exist in S3
     */
    @Override
    public StorageResource retrieve(String key) {
        String bucket = properties.getS3().getBucket();
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getRequest);
            GetObjectResponse response = responseStream.response();

            String contentType = response.contentType();
            long size = response.contentLength() != null ? response.contentLength() : 0L;
            String originalFilename = response.metadata().get("original-filename");

            return new StorageResource(key, responseStream, contentType, size, originalFilename);
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundStorageException(key);
        } catch (AwsServiceException e) {
            if (e.statusCode() == 404) {
                throw new FileNotFoundStorageException(key);
            }
            log.error("Failed to retrieve file from S3 [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to retrieve file from S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("S3 client error during retrieval [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to retrieve file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file from S3 by storage key.
     *
     * @param key unique storage identifier
     */
    @Override
    public void delete(String key) {
        String bucket = properties.getS3().getBucket();
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.debug("Deleted file from S3 [bucket={}, key={}]", bucket, key);
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Failed to delete file from S3 [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a file exists in S3 by storage key using {@code headObject}.
     *
     * @param key unique storage identifier
     * @return true if object exists, false otherwise
     */
    @Override
    public boolean exists(String key) {
        String bucket = properties.getS3().getBucket();
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (AwsServiceException e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.error("Failed to check file existence in S3 [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to check file existence in S3: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("S3 client error checking file existence [bucket={}, key={}]: {}", bucket, key, e.getMessage());
            throw new StorageException("Failed to check file existence in S3: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a unique UUID-based storage key preserving the extension.
     *
     * @param originalFilename original filename hint
     * @return safe UUID storage key
     */
    private String generateKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
