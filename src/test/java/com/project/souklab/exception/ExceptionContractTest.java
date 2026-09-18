package com.project.souklab.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionContractTest {

    @Test
    void supportsResourceNotFoundConstructors() {
        Throwable cause = new IllegalStateException("cause");
        ResourceNotFoundException byField = new ResourceNotFoundException("User", "id", 7);
        ResourceNotFoundException withCause = new ResourceNotFoundException("missing", cause);

        assertThat(byField.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(byField.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(byField.getMessage()).contains("User", "id", "7");
        assertThat(withCause.getCause()).isSameAs(cause);
    }

    @Test
    void supportsAppExceptionOverloads() {
        Throwable cause = new IllegalArgumentException("cause");
        AppException statusFirst = new AppException(HttpStatus.CONFLICT, "CONFLICT_CODE", "conflict");
        AppException statusMessage = new AppException(HttpStatus.BAD_REQUEST, "bad");
        AppException messageStatus = new AppException("forbidden", HttpStatus.FORBIDDEN);
        AppException messageStatusCause = new AppException("failed", HttpStatus.INTERNAL_SERVER_ERROR, cause);

        assertThat(statusFirst.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(statusFirst.getErrorCode()).isEqualTo("CONFLICT_CODE");
        assertThat(statusMessage.getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(messageStatus.getErrorCode()).isEqualTo("FORBIDDEN");
        assertThat(messageStatusCause.getCause()).isSameAs(cause);
    }
}
