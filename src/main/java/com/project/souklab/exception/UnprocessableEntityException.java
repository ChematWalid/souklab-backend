package com.project.souklab.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception representing semantic validation failures or unprocessable domain state (HTTP 422).
 */
public class UnprocessableEntityException extends AppException {

    public UnprocessableEntityException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", message);
    }

    public UnprocessableEntityException(String errorCode, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message);
    }
}
