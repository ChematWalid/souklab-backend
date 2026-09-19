package com.project.souklab.exception;

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

    public AppException(HttpStatus status, String message) {
        this(status, status.toString(), message);
    }

    public AppException(String message, HttpStatus status) {
        this(status, status.toString(), message);
    }

    public AppException(String message, HttpStatus status, Throwable cause) {
        this(status, status.toString(), message, cause);
    }
}
