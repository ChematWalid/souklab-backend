package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, message);
    }

    public ConflictException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, message, cause);
    }
}
