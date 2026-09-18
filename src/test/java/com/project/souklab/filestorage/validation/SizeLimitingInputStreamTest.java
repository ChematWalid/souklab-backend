package com.project.souklab.filestorage.validation;

import com.project.souklab.filestorage.exception.FileTooLargeException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SizeLimitingInputStreamTest {
    @Test
    void tracksSingleReadsSkipsAndMarkReset() throws Exception {
        SizeLimitingInputStream stream = new SizeLimitingInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4}), 4);
        assertThat(stream.getMaxBytes()).isEqualTo(4);
        assertThat(stream.read()).isEqualTo(1);
        stream.mark(10);
        assertThat(stream.skip(1)).isEqualTo(1);
        assertThat(stream.getBytesRead()).isEqualTo(2);
        stream.reset();
        assertThat(stream.getBytesRead()).isEqualTo(1);
        byte[] buffer = new byte[3];
        assertThat(stream.read(buffer, 0, 3)).isEqualTo(3);
        assertThat(stream.read()).isEqualTo(-1);
    }

    @Test
    void rejectsPayloadAfterConfiguredLimit() {
        SizeLimitingInputStream stream = new SizeLimitingInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}), 2);
        assertThatThrownBy(() -> stream.read(new byte[3], 0, 3))
                .isInstanceOf(FileTooLargeException.class);
    }
}
