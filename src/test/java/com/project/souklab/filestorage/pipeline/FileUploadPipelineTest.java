package com.project.souklab.filestorage.pipeline;

import com.project.souklab.exception.BadRequestException;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadPipelineTest {

    @Mock
    private FileValidator fileValidator;

    @Mock
    private VirusScanService virusScanService;

    @Mock
    private StorageService storageService;

    @Mock
    private FileUrlResolver fileUrlResolver;

    @InjectMocks
    private FileUploadPipeline pipeline;

    @Test
    @DisplayName("upload: rejects null or empty file with BadRequestException")
    void upload_emptyFile_throwsBadRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
        FileUploadPolicy policy = FileUploadPolicy.of(1024, List.of("image/png"));

        assertThatThrownBy(() -> pipeline.upload(null, policy))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> pipeline.upload(emptyFile, policy))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("upload: rejects file exceeding max policy size")
    void upload_exceedingSize_throwsFileTooLarge() {
        byte[] payload = new byte[2048];
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", payload);
        FileUploadPolicy policy = FileUploadPolicy.of(1024, List.of("image/png"));

        assertThatThrownBy(() -> pipeline.upload(file, policy))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    @DisplayName("upload: processes valid file through validation, virus scan, and storage")
    void upload_validFile_success() {
        byte[] payload = "valid-content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", payload);
        FileUploadPolicy policy = FileUploadPolicy.of(5000, List.of("image/png"));

        ValidatedFile validatedFile = new ValidatedFile(
                new ByteArrayInputStream(payload),
                "photo.png",
                "image/png",
                payload.length
        );

        when(fileValidator.validateAndSanitize(any(InputStream.class), anyString(), anyString(), anyLong(), anyList()))
                .thenReturn(validatedFile);
        when(virusScanService.scan(validatedFile)).thenReturn(validatedFile);
        when(storageService.store(any(InputStream.class), eq("photo.png"), eq("image/png"), eq((long) payload.length)))
                .thenReturn(new StorageResult("storage-uuid-123", "photo.png", "image/png", payload.length, Instant.now()));
        when(fileUrlResolver.toUrl("storage-uuid-123")).thenReturn("/api/v1/files/storage-uuid-123");

        StoredFileResult result = pipeline.upload(file, policy);

        assertThat(result.storageKey()).isEqualTo("storage-uuid-123");
        assertThat(result.fileUrl()).isEqualTo("/api/v1/files/storage-uuid-123");
        assertThat(result.sanitizedFilename()).isEqualTo("photo.png");
        assertThat(result.detectedMimeType()).isEqualTo("image/png");
        assertThat(result.sizeBytes()).isEqualTo(payload.length);
    }

    @Test
    @DisplayName("upload: executes compensating storage delete if post-storage step fails")
    void upload_postStorageFailure_triggersRollback() {
        byte[] payload = "rollback-test".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "err.png", "image/png", payload);
        FileUploadPolicy policy = FileUploadPolicy.of(5000, List.of("image/png"));

        ValidatedFile validatedFile = new ValidatedFile(
                new ByteArrayInputStream(payload),
                "err.png",
                "image/png",
                payload.length
        );

        when(fileValidator.validateAndSanitize(any(InputStream.class), anyString(), anyString(), anyLong(), anyList()))
                .thenReturn(validatedFile);
        when(virusScanService.scan(validatedFile)).thenReturn(validatedFile);
        when(storageService.store(any(InputStream.class), eq("err.png"), eq("image/png"), eq((long) payload.length)))
                .thenReturn(new StorageResult("storage-fail-key", "err.png", "image/png", payload.length, Instant.now()));
        when(fileUrlResolver.toUrl("storage-fail-key")).thenThrow(new RuntimeException("Simulated URL resolve error"));

        assertThatThrownBy(() -> pipeline.upload(file, policy))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated URL resolve error");

        verify(storageService).delete("storage-fail-key");
    }
}
