package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, message, cause);
    }
}
