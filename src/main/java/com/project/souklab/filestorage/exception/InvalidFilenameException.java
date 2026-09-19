package com.project.souklab.filestorage.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an uploaded filename contains path traversal sequences or illegal control characters.
 */
public class InvalidFilenameException extends StorageException {

    public InvalidFilenameException(String message) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_FILENAME, message);
    }
}
