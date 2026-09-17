package com.project.souklab.filestorage.config;

import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.image.ImageProcessingService;
import com.project.souklab.filestorage.image.ThumbnailatorImageProcessingService;
import com.project.souklab.filestorage.s3.S3StorageService;
import com.project.souklab.filestorage.scan.ClamdInstreamScanner;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.scan.VirusScanner;
import com.project.souklab.filestorage.stub.InMemoryStorageService;
import com.project.souklab.filestorage.validation.FileValidator;
import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Clock;

/**
 * Spring auto-configuration for the file storage module.
 * Provides beans for Tika MIME sniffing, file validation, antivirus scanning,
 * image processing, and conditionally registers either in-memory or S3-compatible backend storage services.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    /**
     * Provides an Apache Tika instance for MIME type sniffing via content magic bytes.
     *
     * @return a configured Tika instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Tika tika() {
        return new Tika();
    }

    /**
     * Provides the provider-agnostic file validator and sanitizer component.
     * Validates that max file size and allowed MIME types are configured,
     * failing fast at startup with an {@link IllegalStateException} if missing or empty.
     *
     * @param properties configuration properties for file validation
     * @param tika Apache Tika detector instance
     * @return a configured FileValidator
     * @throws IllegalStateException if validation properties are missing or empty
     */
    @Bean
    @ConditionalOnMissingBean
    public FileValidator fileValidator(StorageProperties properties, Tika tika) {
        if (properties == null || properties.getValidation() == null
                || properties.getValidation().getMaxFileSize() == null) {
            throw new IllegalStateException("storage.validation.max-file-size is required");
        }
        if (properties.getValidation().getAllowedMimeTypes() == null
                || properties.getValidation().getAllowedMimeTypes().isEmpty()) {
            throw new IllegalStateException("storage.validation.allowed-mime-types is required and cannot be empty");
        }
        return new FileValidator(properties, tika);
    }

    /**
     * Registers the in-memory storage service bean when {@code storage.provider=in-memory} (or omitted).
     *
     * @param clock application Clock bean used for storage timestamps
     * @return an InMemoryStorageService bean
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "in-memory", matchIfMissing = true)
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService inMemoryStorageService(Clock clock) {
        return new InMemoryStorageService(clock);
    }

    /**
     * Registers the AWS SDK v2 {@link S3Client} when {@code storage.provider=s3}.
     * Validates that access key, secret key, and bucket are present, failing fast at startup
     * with an {@link IllegalStateException} if any is missing or blank.
     *
     * @param properties file storage properties containing S3 settings
     * @return a configured S3Client
     * @throws IllegalStateException if credentials or bucket are missing or blank
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(StorageProperties properties) {
        StorageProperties.S3Properties s3 = properties.getS3();
        if (s3 == null
                || s3.getAccessKey() == null || s3.getAccessKey().isBlank()
                || s3.getSecretKey() == null || s3.getSecretKey().isBlank()) {
            throw new IllegalStateException("storage.s3.access-key and storage.s3.secret-key are required when storage.provider=s3");
        }
        if (s3.getBucket() == null || s3.getBucket().isBlank()) {
            throw new IllegalStateException("storage.s3.bucket is required when storage.provider=s3");
        }

        if (s3.getRegion() == null || s3.getRegion().isBlank()) {
            throw new IllegalStateException("storage.s3.region is required when storage.provider=s3");
        }
        boolean pathStyle = Boolean.TRUE.equals(s3.getPathStyleAccess());

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build());

        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * Registers the S3 storage service backend bean when {@code storage.provider=s3}.
     *
     * @param properties file storage properties
     * @param s3Client the configured S3Client
     * @param clock application Clock bean used for storage timestamps
     * @return an S3StorageService bean
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService s3StorageService(StorageProperties properties, S3Client s3Client, Clock clock) {
        return new S3StorageService(properties, s3Client, clock);
    }

    /**
     * Registers the default image processing service bean using Thumbnailator.
     *
     * @return an ImageProcessingService bean
     */
    @Bean
    @ConditionalOnMissingBean(ImageProcessingService.class)
    public ImageProcessingService imageProcessingService() {
        return new ThumbnailatorImageProcessingService();
    }

    /**
     * Registers the ClamAV daemon INSTREAM scanner bean.
     * Validates that host and positive port are configured if virus scanning is enabled,
     * failing fast at startup with an {@link IllegalStateException} if missing or invalid.
     *
     * @param properties file storage configuration properties
     * @return a configured ClamdInstreamScanner bean
     * @throws IllegalStateException if virus scanning is enabled but host or port is invalid
     */
    @Bean
    @ConditionalOnMissingBean(VirusScanner.class)
    public VirusScanner virusScanner(StorageProperties properties) {
        StorageProperties.VirusScanProperties scanProps = properties.getVirusScan();
        if (scanProps != null && scanProps.isEnabled()) {
            if (scanProps.getHost() == null || scanProps.getHost().isBlank()) {
                throw new IllegalStateException("storage.virus-scan.host is required when storage.virus-scan.enabled=true");
            }
            if (scanProps.getPort() <= 0) {
                throw new IllegalStateException("storage.virus-scan.port must be positive when storage.virus-scan.enabled=true");
            }
        }
        return new ClamdInstreamScanner(scanProps != null ? scanProps : new StorageProperties.VirusScanProperties());
    }

    /**
     * Registers the virus scanning coordinator service bean.
     *
     * @param properties file storage configuration properties
     * @param virusScanner the configured VirusScanner implementation
     * @return a configured VirusScanService bean
     */
    @Bean
    @ConditionalOnMissingBean(VirusScanService.class)
    public VirusScanService virusScanService(StorageProperties properties, VirusScanner virusScanner) {
        return new VirusScanService(properties, virusScanner);
    }
}
