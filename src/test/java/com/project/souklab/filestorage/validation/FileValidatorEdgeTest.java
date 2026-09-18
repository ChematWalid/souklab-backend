package com.project.souklab.filestorage.validation;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.InvalidFilenameException;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class FileValidatorEdgeTest {
    @Test
    void validatesConstructorAndByteArrayGuards() {
        assertThatThrownBy(() -> new FileValidator(null, new Tika())).isInstanceOf(IllegalStateException.class);
        StorageProperties missingSize = new StorageProperties();
        missingSize.getValidation().setMaxFileSize(null);
        assertThatThrownBy(() -> new FileValidator(missingSize, new Tika())).isInstanceOf(IllegalStateException.class);
        StorageProperties missingTypes = validProperties();
        missingTypes.getValidation().setAllowedMimeTypes(List.of());
        assertThatThrownBy(() -> new FileValidator(missingTypes, new Tika())).isInstanceOf(IllegalStateException.class);
        StorageProperties nullTypes = validProperties();
        nullTypes.getValidation().setAllowedMimeTypes(null);
        assertThatThrownBy(() -> new FileValidator(nullTypes, new Tika())).isInstanceOf(IllegalStateException.class);
        FileValidator validator = new FileValidator(validProperties(), new Tika());
        assertThatThrownBy(() -> validator.validateAndSanitize((byte[]) null, "a.jpg", "image/jpeg"))
                .isInstanceOf(UnsupportedFileTypeException.class);
        assertThatThrownBy(() -> validator.validateAndSanitize(new byte[0], "a.jpg", "image/jpeg"))
                .isInstanceOf(UnsupportedFileTypeException.class);
        assertThatThrownBy(() -> validator.validateAndSanitize((InputStream) null, "a.jpg", "image/jpeg", 1))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void sanitizesSafeNamesAndRejectsDangerousNames() {
        FileValidator validator = new FileValidator(validProperties(), new Tika());
        assertThat(validator.sanitizeFilename("C:\\temp\\..\\photo file.jpg")).isEqualTo("photo_file.jpg");
        assertThat(validator.sanitizeFilename("/tmp/photo.jpg")).isEqualTo("photo.jpg");
        assertThatThrownBy(() -> validator.sanitizeFilename(null)).isInstanceOf(InvalidFilenameException.class);
        assertThatThrownBy(() -> validator.sanitizeFilename(" ")).isInstanceOf(InvalidFilenameException.class);
        assertThatThrownBy(() -> validator.sanitizeFilename("bad\0name.jpg")).isInstanceOf(InvalidFilenameException.class);
        assertThatThrownBy(() -> validator.sanitizeFilename("..")).isInstanceOf(InvalidFilenameException.class);
        assertThatThrownBy(() -> validator.sanitizeFilename("...")).isInstanceOf(InvalidFilenameException.class);
        assertThatThrownBy(() -> validator.sanitizeFilename("_")).isInstanceOf(InvalidFilenameException.class);
    }

    @Test
    void handlesAliasesCustomListsBlankHeadersAndSizeLimits() throws Exception {
        StorageProperties properties = validProperties();
        Tika tika = Mockito.mock(Tika.class);
        when(tika.detect(any(InputStream.class), anyString())).thenReturn("image/jpeg");
        FileValidator validator = new FileValidator(properties, tika);

        ValidatedFile aliased = validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpg; charset=binary", 1);
        assertThat(aliased.detectedMimeType()).isEqualTo("image/jpeg");
        assertThat(validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", " ", 1)).isNotNull();
        assertThat(validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of("image/jpeg"))).isNotNull();
        assertThat(validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, null)).isNotNull();
        assertThat(validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", null, 1)).isNotNull();
        assertThat(validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of())).isNotNull();
        assertThatThrownBy(() -> validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 2))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void wrapsMimeDetectionIoFailuresAndRejectsDisallowedCustomTypes() throws Exception {
        StorageProperties properties = validProperties();
        Tika tika = Mockito.mock(Tika.class);
        when(tika.detect(any(InputStream.class), anyString())).thenThrow(new IOException("sniff failed"));
        FileValidator validator = new FileValidator(properties, tika);
        assertThatThrownBy(() -> validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1))
                .isInstanceOf(UnsupportedFileTypeException.class);

        Tika disallowedTika = Mockito.mock(Tika.class);
        when(disallowedTika.detect(any(InputStream.class), eq("a.jpg"))).thenReturn("application/pdf");
        FileValidator disallowed = new FileValidator(properties, disallowedTika);
        assertThatThrownBy(() -> disallowed.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "application/pdf", 1, List.of("image/jpeg")))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void coversMutableConfigurationAndStreamHandlingBranches() throws Exception {
        StorageProperties properties = validProperties();
        Tika tika = Mockito.mock(Tika.class);
        when(tika.detect(any(InputStream.class), anyString())).thenReturn("image/jpeg");
        FileValidator validator = new FileValidator(properties, tika);

        properties.setValidation(null);
        assertThatThrownBy(() -> validator.validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of("image/jpeg")))
                .isInstanceOf(IllegalStateException.class);

        StorageProperties nullMax = validProperties();
        FileValidator nullMaxValidator = new FileValidator(nullMax, tika);
        nullMax.getValidation().setMaxFileSize(null);
        assertThatThrownBy(() -> nullMaxValidator.validateAndSanitize(
                new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of("image/jpeg")))
                .isInstanceOf(IllegalStateException.class);

        StorageProperties emptyTypes = validProperties();
        FileValidator emptyTypeValidator = new FileValidator(emptyTypes, tika);
        emptyTypes.getValidation().setAllowedMimeTypes(List.of());
        assertThatThrownBy(() -> emptyTypeValidator.validateAndSanitize(
                new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        StorageProperties nullFallbackTypes = validProperties();
        FileValidator nullFallbackValidator = new FileValidator(nullFallbackTypes, tika);
        nullFallbackTypes.getValidation().setAllowedMimeTypes(null);
        assertThatThrownBy(() -> nullFallbackValidator.validateAndSanitize(
                new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        InputStream noMark = new InputStream() {
            private final InputStream delegate = new ByteArrayInputStream(new byte[]{1});
            @Override public int read() throws IOException { return delegate.read(); }
            @Override public boolean markSupported() { return false; }
        };
        StorageProperties streamProperties = validProperties();
        FileValidator streamValidator = new FileValidator(streamProperties, tika);
        assertThat(streamValidator.validateAndSanitize(noMark, "a.jpg", "image/jpeg", 1)).isNotNull();

        Tika reverseAliasTika = Mockito.mock(Tika.class);
        when(reverseAliasTika.detect(any(InputStream.class), anyString())).thenReturn("image/jpg");
        assertThat(new FileValidator(validProperties(), reverseAliasTika)
                .validateAndSanitize(new ByteArrayInputStream(new byte[]{1}), "a.jpg", "image/jpeg", 1,
                        List.of("image/jpeg", "image/jpg")))
                .isNotNull();
    }

    private StorageProperties validProperties() {
        StorageProperties properties = new StorageProperties();
        properties.getValidation().setMaxFileSize(DataSize.ofBytes(1));
        properties.getValidation().setAllowedMimeTypes(List.of("image/jpeg"));
        return properties;
    }
}
