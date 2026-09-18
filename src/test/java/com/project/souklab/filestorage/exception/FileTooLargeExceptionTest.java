package com.project.souklab.filestorage.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileTooLargeExceptionTest {
    @Test
    void stringConstructorPreservesStorageErrorContract() {
        FileTooLargeException exception = new FileTooLargeException("too large");

        assertThat(exception.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("FILE_TOO_LARGE");
        assertThat(exception.getMessage()).isEqualTo("too large");
    }
}
