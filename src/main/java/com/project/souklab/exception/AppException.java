package com.project.souklab.exception;

import com.project.souklab.dto.common.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AppException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AppException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AppException(HttpStatus status, ApiErrorCode errorCode, String message) {
        this(status, errorCode.value(), message);
    }

    public AppException(HttpStatus status, ApiErrorCode errorCode, String message, Throwable cause) {
        this(status, errorCode.value(), message, cause);
    }

    public AppException(HttpStatus status, String message) {
        this(status, errorCode(status), message);
    }

    public AppException(String message, HttpStatus status) {
        this(status, errorCode(status), message);
    }

    public AppException(String message, HttpStatus status, Throwable cause) {
        this(status, errorCode(status), message, cause);
    }

    private static String errorCode(HttpStatus status) {
        return status.toString().replaceFirst("^\\d+\\s+", "");
    }
}
