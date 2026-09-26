package com.project.souklab.filestorage.pipeline;

/**
 * Result descriptor containing the storage key, public serving URL, and sanitized metadata
 * produced by {@link FileUploadPipeline}.
 */
public record StoredFileResult(
        String storageKey,
        String fileUrl,
        String sanitizedFilename,
        String detectedMimeType,
        long sizeBytes
) {
}
