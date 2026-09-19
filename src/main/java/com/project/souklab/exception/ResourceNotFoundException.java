package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, message, cause);
    }
}
