package com.project.souklab.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionSubclassTest {
    @Test
    void exposesTypedStatusAndCause() {
        Throwable cause = new IllegalStateException("cause");
        assertException(new BadRequestException("bad", cause), HttpStatus.BAD_REQUEST, "BAD_REQUEST", cause);
        assertException(new ConflictException("conflict", cause), HttpStatus.CONFLICT, "CONFLICT", cause);
        assertException(new ForbiddenException("forbidden", cause), HttpStatus.FORBIDDEN, "FORBIDDEN", cause);
        assertException(new UnauthorizedException("unauthorized", cause), HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", cause);
    }

    private void assertException(AppException exception, HttpStatus status, String code, Throwable cause) {
        assertThat(exception.getStatus()).isEqualTo(status);
        assertThat(exception.getErrorCode()).isEqualTo(code);
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
