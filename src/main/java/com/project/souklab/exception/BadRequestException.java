package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message, cause);
    }
}
