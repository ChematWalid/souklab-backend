package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, message, cause);
    }
}
