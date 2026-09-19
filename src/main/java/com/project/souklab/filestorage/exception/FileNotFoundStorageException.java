package com.project.souklab.filestorage.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested storage key does not exist.
 */
public class FileNotFoundStorageException extends StorageException {

    public FileNotFoundStorageException(String key) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.FILE_NOT_FOUND, String.format("File not found for key: %s", key));
    }
}
