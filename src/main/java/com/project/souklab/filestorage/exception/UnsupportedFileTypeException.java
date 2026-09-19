package com.project.souklab.filestorage.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an uploaded file fails MIME validation (either disallowed type or content spoofing).
 */
public class UnsupportedFileTypeException extends StorageException {

    public UnsupportedFileTypeException(String detectedMimeType) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.UNSUPPORTED_FILE_TYPE,
                String.format("File type '%s' is not allowed", detectedMimeType));
    }

    public UnsupportedFileTypeException(String detectedMimeType, String declaredContentType) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.UNSUPPORTED_FILE_TYPE,
                String.format("Declared content type '%s' does not match actual file content '%s'", declaredContentType, detectedMimeType));
    }

    public UnsupportedFileTypeException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.UNSUPPORTED_FILE_TYPE, message, cause);
    }
}
