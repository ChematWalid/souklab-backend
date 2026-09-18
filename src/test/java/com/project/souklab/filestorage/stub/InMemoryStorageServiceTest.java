package com.project.souklab.filestorage.stub;

import com.project.souklab.filestorage.exception.FileNotFoundStorageException;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.StorageException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class InMemoryStorageServiceTest {
    @Test
    void storesMetadataAndHandlesExtensions() throws Exception {
        InMemoryStorageService service = new InMemoryStorageService(
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        var result = service.store(new ByteArrayInputStream(new byte[]{1, 2}), "PHOTO.JPG", "image/jpeg", 2);
        assertThat(result.key()).endsWith(".jpg");
        assertThat(service.exists(result.key())).isTrue();
        assertThat(service.retrieve(result.key()).content().readAllBytes()).containsExactly(1, 2);
        service.delete(result.key());
        service.clear();
        assertThat(service.exists(result.key())).isFalse();
    }

    @Test
    void validatesNullAndMissingContentAndPreservesSizeLimitFailures() {
        InMemoryStorageService service = new InMemoryStorageService(Clock.systemUTC());
        assertThatThrownBy(() -> service.store(null, "file.pdf", "application/pdf", 0))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> service.retrieve("missing"))
                .isInstanceOf(FileNotFoundStorageException.class);
        assertThatCode(() -> service.store(new ByteArrayInputStream(new byte[]{1}), null, "x", 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.store(new ByteArrayInputStream(new byte[]{1}), "file.", "x", 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.store(new ByteArrayInputStream(new byte[]{1}), "file", "x", 1))
                .doesNotThrowAnyException();
        var limited = new com.project.souklab.filestorage.validation.SizeLimitingInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}), 1);
        assertThatThrownBy(() -> service.store(limited, "file", "x", 2))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void wrapsUnexpectedReadFailuresAndRejectsNullClock() {
        assertThatThrownBy(() -> new InMemoryStorageService(null))
                .isInstanceOf(IllegalArgumentException.class);

        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read failure");
            }
        };
        InMemoryStorageService service = new InMemoryStorageService(Clock.systemUTC());
        assertThatThrownBy(() -> service.store(broken, ".hidden", "text/plain", 1))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to read input content stream");
    }

    @Test
    void preservesSizeLimitFailureWrappedAsAnIoCause() {
        InMemoryStorageService service = new InMemoryStorageService(Clock.systemUTC());
        FileTooLargeException limitFailure = new FileTooLargeException(2, 1);
        InputStream wrappedLimitFailure = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("size limit", limitFailure);
            }
        };

        assertThatThrownBy(() -> service.store(wrappedLimitFailure, "file.bin", "application/octet-stream", 2))
                .isSameAs(limitFailure);
    }
}
