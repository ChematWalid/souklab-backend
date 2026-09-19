package com.project.souklab.filestorage.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base domain exception for all file storage operations.
 * Encapsulates an HTTP status and machine-readable error code for API error reporting.
 */
@Getter
public class StorageException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public StorageException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public StorageException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public StorageException(HttpStatus status, ApiErrorCode errorCode, String message) {
        this(status, errorCode.value(), message);
    }

    public StorageException(HttpStatus status, ApiErrorCode errorCode, String message, Throwable cause) {
        this(status, errorCode.value(), message, cause);
    }

    public StorageException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.STORAGE_ERROR, message);
    }

    public StorageException(String message, Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.STORAGE_ERROR, message, cause);
    }
}
