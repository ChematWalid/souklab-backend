package com.project.souklab.filestorage.validation;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.InvalidFilenameException;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provider-agnostic file validator and sanitizer component.
 * Executes size checking, magic-byte MIME type sniffing (via Apache Tika),
 * Content-Type spoof detection, and strict filename sanitization.
 *
 * <p><strong>Stream Buffering &amp; Memory Strategy:</strong>
 * Uses a fixed 16KB bounded mark/reset buffer (via {@link BufferedInputStream}) for Tika magic-byte sniffing.
 * The entire stream is NEVER loaded into memory during validation, keeping heap consumption O(1) (~16KB)
 * even when validating multi-megabyte/gigabyte files for future S3/MinIO streaming.
 * In addition, the stream is wrapped in a {@link SizeLimitingInputStream} hard-capped at {@code maxFileSize} bytes,
 * preventing memory or disk exhaustion if a client declares a small size but sends a larger payload.
 *
 * <p><strong>Stream Consumption &amp; Invariant Rule:</strong>
 * Any {@link StorageService} implementation MUST consume the
 * {@link ValidatedFile#content()} wrapped stream (the stream that was already wrapped in
 * {@link SizeLimitingInputStream}) and MUST NEVER re-wrap or substitute the original raw stream.
 * The hard size-cap and memory-exhaustion defenses strictly depend on this invariant being respected
 * by every storage backend implementation (including the in-memory stub and S3/MinIO backends).
 *
 * <p><strong>CRITICAL ARCHITECTURAL RULE:</strong>
 * The sanitized filename produced by this component must NEVER be used as a literal storage path segment.
 * Storage keys must always be generated safe identifiers (e.g. UUID-based) to guarantee path isolation.
 */
@Component
@Slf4j
public class FileValidator {

    private static final int DETECTION_BUFFER_SIZE = 8192;

    private final StorageProperties properties;
    private final Tika tika;

    /**
     * Constructs a FileValidator, failing fast at startup if required validation properties are missing or empty.
     *
     * @param properties configuration properties for file validation
     * @param tika Apache Tika detector instance
     * @throws IllegalStateException if max-file-size or allowed-mime-types configuration is missing or empty
     */
    public FileValidator(StorageProperties properties, Tika tika) {
        if (properties == null || properties.getValidation() == null
                || properties.getValidation().getMaxFileSize() == null) {
            throw new IllegalStateException("storage.validation.max-file-size is required");
        }
        if (properties.getValidation().getAllowedMimeTypes() == null
                || properties.getValidation().getAllowedMimeTypes().isEmpty()) {
            throw new IllegalStateException("storage.validation.allowed-mime-types is required and cannot be empty");
        }
        this.properties = properties;
        this.tika = tika;
    }

    /**
     * Validates and sanitizes a byte array file upload by delegating to
     * {@link #validateAndSanitize(InputStream, String, String, long)}.
     *
     * @param bytes byte array content
     * @param originalFilename original filename hint
     * @param declaredContentType declared Content-Type header
     * @return ValidatedFile container
     */
    public ValidatedFile validateAndSanitize(byte[] bytes, String originalFilename, String declaredContentType) {
        if (bytes == null || bytes.length == 0) {
            throw new UnsupportedFileTypeException("Uploaded file content cannot be empty");
        }
        return validateAndSanitize(new ByteArrayInputStream(bytes), originalFilename, declaredContentType, bytes.length);
    }

    /**
     * Validates and sanitizes an InputStream file upload through a seven-step pipeline:
     * <ol>
     *   <li>Initial size validation against declared size.</li>
     *   <li>Stream wrapping in {@link SizeLimitingInputStream} to hard-cap reads at {@code maxFileSize}.</li>
     *   <li>Filename sanitization against path traversal and forbidden characters.</li>
     *   <li>Preparation of a markable stream for magic-byte sniffing (fixed 16KB bounded buffer).</li>
     *   <li>Content-based MIME detection via Apache Tika magic bytes.</li>
     *   <li>Allowed MIME types list verification.</li>
     *   <li>Anti-spoofing comparison between detected MIME type and declared Content-Type header.</li>
     * </ol>
     *
     * @param content raw input stream
     * @param originalFilename original filename hint
     * @param declaredContentType declared Content-Type header
     * @param declaredSize declared payload size in bytes
     * @return ValidatedFile containing the wrapped stream and sanitized metadata
     * @throws FileTooLargeException if declared size exceeds configured limit
     * @throws UnsupportedFileTypeException if MIME type is disallowed or spoofed
     * @throws InvalidFilenameException if filename contains path traversal or dangerous characters
     */
    public ValidatedFile validateAndSanitize(InputStream content, String originalFilename, String declaredContentType, long declaredSize) {
        return validateAndSanitize(content, originalFilename, declaredContentType, declaredSize, properties.getValidation().getAllowedMimeTypes());
    }

    /**
     * Validates and sanitizes an InputStream file upload against a caller-specified list of allowed MIME types.
     * Enables domain services with tighter format restrictions (such as avatars restricting strictly to images)
     * to enforce narrowed constraints without bypassing stream size capping, magic-byte sniffing, or spoof detection.
     *
     * @param content raw input stream
     * @param originalFilename original filename hint
     * @param declaredContentType declared Content-Type header
     * @param declaredSize declared payload size in bytes
     * @param customAllowedMimeTypes custom list of permitted MIME types for this upload context
     * @return ValidatedFile containing the wrapped stream and sanitized metadata
     * @throws FileTooLargeException if declared size exceeds configured limit
     * @throws UnsupportedFileTypeException if MIME type is disallowed or spoofed
     * @throws InvalidFilenameException if filename contains path traversal or dangerous characters
     */
    public ValidatedFile validateAndSanitize(InputStream content, String originalFilename, String declaredContentType, long declaredSize, List<String> customAllowedMimeTypes) {
        if (content == null) {
            throw new UnsupportedFileTypeException("Uploaded file stream cannot be null");
        }

        if (properties.getValidation() == null || properties.getValidation().getMaxFileSize() == null) {
            throw new IllegalStateException("storage.validation.max-file-size is required");
        }

        long maxBytes = properties.getValidation().getMaxFileSize().toBytes();
        if (declaredSize > maxBytes) {
            log.warn("File upload rejected: declared size {} bytes exceeds maximum limit {} bytes", declaredSize, maxBytes);
            throw new FileTooLargeException(declaredSize, maxBytes);
        }

        SizeLimitingInputStream boundedStream = new SizeLimitingInputStream(content, maxBytes);
        String sanitizedFilename = sanitizeFilename(originalFilename);

        InputStream stream = boundedStream.markSupported()
                ? boundedStream
                : new BufferedInputStream(boundedStream, DETECTION_BUFFER_SIZE * 2);
        stream.mark(DETECTION_BUFFER_SIZE * 2);

        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(stream, sanitizedFilename);
            stream.reset();
        } catch (IOException e) {
            log.error("Failed to sniff file MIME type from input stream", e);
            throw new UnsupportedFileTypeException("Could not determine file type from content signature", e);
        }

        List<String> allowedTypes = (customAllowedMimeTypes != null && !customAllowedMimeTypes.isEmpty())
                ? customAllowedMimeTypes
                : properties.getValidation().getAllowedMimeTypes();

        if (allowedTypes == null || allowedTypes.isEmpty()) {
            throw new IllegalStateException("storage.validation.allowed-mime-types is required and cannot be empty");
        }
        boolean isAllowed = allowedTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(detectedMimeType));

        if (!isAllowed) {
            log.warn("File upload rejected: sniffed MIME type '{}' is not in allowed list {}", detectedMimeType, allowedTypes);
            throw new UnsupportedFileTypeException(detectedMimeType);
        }

        if (declaredContentType != null && !declaredContentType.isBlank()) {
            String cleanDeclared = declaredContentType.split(";")[0].trim().toLowerCase();
            if (!isMatchingMimeType(cleanDeclared, detectedMimeType)) {
                log.warn("MIME spoofing detected! Sniffed magic bytes: '{}', Declared header: '{}'", detectedMimeType, declaredContentType);
                throw new UnsupportedFileTypeException(detectedMimeType, declaredContentType);
            }
        }

        return new ValidatedFile(stream, sanitizedFilename, detectedMimeType, declaredSize);
    }

    /**
     * Sanitizes an original filename by:
     * <ul>
     *   <li>Stripping directory prefixes (both Unix / and Windows \).</li>
     *   <li>Stripping path traversal sequences (..).</li>
     *   <li>Whitelisting safe characters (alphanumeric, dot, underscore, dash).</li>
     *   <li>Rejecting dangerous or empty outcomes (. or _).</li>
     * </ul>
     *
     * @param filename raw original filename hint
     * @return clean, safe filename
     * @throws InvalidFilenameException if filename is blank, contains null bytes, or resolves to dangerous patterns
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidFilenameException("Filename must not be null or blank");
        }

        if (filename.contains("\0")) {
            throw new InvalidFilenameException("Filename contains illegal null byte character");
        }

        String clean = filename.replace('\\', '/');
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }

        clean = clean.replace("..", "");
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_").trim();

        if (clean.isBlank() || clean.equals(".") || clean.equals("_")) {
            throw new InvalidFilenameException("Filename is invalid or dangerous: " + filename);
        }

        return clean;
    }

    /**
     * Checks if declared and detected MIME types match, accommodating common aliases (e.g. image/jpg vs image/jpeg).
     *
     * @param declared cleaned declared Content-Type
     * @param detected sniffed MIME type from magic bytes
     * @return true if types match or are known aliases, false otherwise
     */
    private boolean isMatchingMimeType(String declared, String detected) {
        if (declared.equalsIgnoreCase(detected)) {
            return true;
        }
        if ((declared.equals("image/jpg") && detected.equalsIgnoreCase("image/jpeg")) ||
            (declared.equals("image/jpeg") && detected.equalsIgnoreCase("image/jpg"))) {
            return true;
        }
        return false;
    }
}
