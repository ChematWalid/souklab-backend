package com.project.souklab.filestorage.scan;

import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.config.OperationalMetrics;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.exception.VirusScanException;
import com.project.souklab.filestorage.validation.ValidatedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service orchestrating virus scanning for uploaded files.
 * Evaluates scan results against configured fail-open and fail-closed policies,
 * buffers stream content in memory up to the configured maxFileSize cap,
 * and produces a refreshed ValidatedFile ready for downstream storage persistence.
 */
public class VirusScanService {

    private static final Logger log = LoggerFactory.getLogger(VirusScanService.class);

    private final StorageProperties properties;
    private final VirusScanner virusScanner;
    private final OperationalMetrics metrics;

    public VirusScanService(StorageProperties properties, VirusScanner virusScanner) {
        this(properties, virusScanner, OperationalMetrics.noop());
    }

    public VirusScanService(StorageProperties properties, VirusScanner virusScanner,
                            OperationalMetrics metrics) {
        if (properties == null) {
            throw new IllegalArgumentException("StorageProperties cannot be null");
        }
        if (virusScanner == null) {
            throw new IllegalArgumentException("VirusScanner cannot be null");
        }
        if (metrics == null) {
            throw new IllegalArgumentException("OperationalMetrics cannot be null");
        }
        this.properties = properties;
        this.virusScanner = virusScanner;
        this.metrics = metrics;
    }

    /**
     * Scans a validated file for viruses and malware.
     * If virus scanning is disabled in configuration, returns the input file directly.
     * If enabled, reads the stream into memory (already bounded by FileValidator),
     * passes it to the VirusScanner, evaluates the result against the fail-open/closed policy,
     * and returns a new ValidatedFile with a fresh, unconsumed stream.
     *
     * @param file the validated file to scan
     * @return a ValidatedFile whose stream is positioned at the start, ready for storage persistence
     * @throws VirusDetectedException if malware signature is detected (always thrown, regardless of fail-open)
     * @throws VirusScanException if scanner communication fails and fail-open policy is false
     */
    public ValidatedFile scan(ValidatedFile file) {
        if (file == null) {
            throw new IllegalArgumentException("ValidatedFile cannot be null");
        }

        if (properties.getVirusScan() == null || !properties.getVirusScan().isEnabled()) {
            log.debug("Virus scanning is disabled. Skipping scan for '{}'", file.sanitizedFilename());
            return file;
        }

        byte[] bytes;
        try {
            bytes = file.content().readAllBytes();
        } catch (IOException e) {
            log.error("Failed to buffer validated stream for virus scanning: {}", file.sanitizedFilename(), e);
            throw new VirusScanException("Failed to read file content for virus scanning: " + e.getMessage(), e);
        }

        performScan(new ByteArrayInputStream(bytes), file.sanitizedFilename());

        return new ValidatedFile(
                new ByteArrayInputStream(bytes),
                file.sanitizedFilename(),
                file.detectedMimeType(),
                bytes.length
        );
    }

    /**
     * Scans a byte array directly, applying virus detection and fail-open/closed policies.
     *
     * @param bytes the file bytes to scan
     * @param filename filename hint for logging and reporting
     * @throws VirusDetectedException if malware signature is detected
     * @throws VirusScanException if scanner communication fails and fail-open policy is false
     */
    public void scanBytes(byte[] bytes, String filename) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array content cannot be null");
        }
        if (properties.getVirusScan() == null || !properties.getVirusScan().isEnabled()) {
            log.debug("Virus scanning is disabled. Skipping scan for '{}'", filename);
            return;
        }
        performScan(new ByteArrayInputStream(bytes), filename);
    }

    /**
     * Scans an InputStream directly, applying virus detection and fail-open/closed policies.
     *
     * @param content stream to scan
     * @param filename filename hint for logging and reporting
     * @throws VirusDetectedException if malware signature is detected
     * @throws VirusScanException if scanner communication fails and fail-open policy is false
     */
    public void scanStream(InputStream content, String filename) {
        if (content == null) {
            throw new IllegalArgumentException("Content stream cannot be null");
        }
        if (properties.getVirusScan() == null || !properties.getVirusScan().isEnabled()) {
            log.debug("Virus scanning is disabled. Skipping scan for '{}'", filename);
            return;
        }
        performScan(content, filename);
    }

    /**
     * Executes the scan and enforces malware rejection and failure policies.
     *
     * @param content stream to scan
     * @param filename filename hint for logging
     */
    private void performScan(InputStream content, String filename) {
        ScanResult result;
        try {
            result = virusScanner.scan(content);
        } catch (RuntimeException ex) {
            metrics.recordVirusScan(AnalyticsMetric.Operational.Outcome.ERROR.value());
            throw ex;
        }

        if (result.isInfected()) {
                metrics.recordVirusScan(AnalyticsMetric.Operational.Outcome.INFECTED.value());
            log.error("Malware detected in upload '{}': virus='{}'", filename, result.virusName());
            throw new VirusDetectedException(result.virusName());
        }

        if (result.isError()) {
            boolean failOpen = properties.getVirusScan().isFailOpen();
            if (failOpen) {
                metrics.recordVirusScan(AnalyticsMetric.Operational.Outcome.ERROR_ALLOWED.value());
                log.warn("Virus scanner communication failure for '{}': {}. Fail-open policy active: allowing upload to proceed.",
                        filename, result.message());
            } else {
                metrics.recordVirusScan(AnalyticsMetric.Operational.Outcome.ERROR_REJECTED.value());
                log.error("Virus scanner communication failure for '{}': {}. Fail-closed policy active: rejecting upload.",
                        filename, result.message());
                throw new VirusScanException("Virus scanning service unavailable: " + result.message());
            }
        } else {
                metrics.recordVirusScan(AnalyticsMetric.Operational.Outcome.CLEAN.value());
        }
    }
}
