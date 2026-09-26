package com.project.souklab.filestorage.pipeline;

import java.util.List;

/**
 * Validation and constraint policy for incoming file uploads processed by {@link FileUploadPipeline}.
 */
public record FileUploadPolicy(
        long maxFileSizeBytes,
        List<String> allowedMimeTypes
) {
    public static FileUploadPolicy of(long maxFileSizeBytes, List<String> allowedMimeTypes) {
        return new FileUploadPolicy(maxFileSizeBytes, allowedMimeTypes);
    }
}
