package com.project.souklab.filestorage.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an uploaded file exceeds the configured maximum allowed size limit.
 */
public class FileTooLargeException extends StorageException {

    public FileTooLargeException(long actualSize, long maxSize) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.FILE_TOO_LARGE,
                String.format("File size (%d bytes) exceeds the maximum allowed limit of %d bytes", actualSize, maxSize));
    }

    public FileTooLargeException(String message) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.FILE_TOO_LARGE, message);
    }
}
