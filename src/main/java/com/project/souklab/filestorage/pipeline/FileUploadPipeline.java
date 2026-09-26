package com.project.souklab.filestorage.pipeline;

import com.project.souklab.exception.BadRequestException;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Reusable, multi-stage upload pipeline encapsulating:
 * <ol>
 *   <li>Presence and payload validation</li>
 *   <li>Declared and stream size bound verification</li>
 *   <li>MIME inspection and filename sanitization via {@link FileValidator}</li>
 *   <li>Antivirus scanning via {@link VirusScanService}</li>
 *   <li>Physical persistence via {@link StorageService}</li>
 *   <li>Rollback compensation on failure</li>
 *   <li>Public/CDN file URL resolution via {@link FileUrlResolver}</li>
 * </ol>
 * Eliminates duplicate file handling boilerplate across avatar, gallery, certification,
 * and syllabus upload features.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadPipeline {

    private final FileValidator fileValidator;
    private final VirusScanService virusScanService;
    private final StorageService storageService;
    private final FileUrlResolver fileUrlResolver;

    /**
     * Executes the full upload lifecycle for a multipart payload.
     *
     * @param file   the multipart file from the controller
     * @param policy upload constraints including max size and permitted MIME types
     * @return descriptor with storage key, resolved public URL, and metadata
     * @throws BadRequestException      if the file is null or empty
     * @throws FileTooLargeException    if the file exceeds the policy limit
     * @throws StorageException        if stream reading or storage writes fail
     */
    public StoredFileResult upload(MultipartFile file, FileUploadPolicy policy) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Upload file is required and cannot be empty.");
        }

        if (policy != null && policy.maxFileSizeBytes() > 0 && file.getSize() > policy.maxFileSizeBytes()) {
            throw new FileTooLargeException(file.getSize(), policy.maxFileSizeBytes());
        }

        ValidatedFile validatedFile;
        try {
            validatedFile = fileValidator.validateAndSanitize(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    policy != null ? policy.allowedMimeTypes() : null
            );
        } catch (IOException e) {
            log.error("Failed to read file upload stream for '{}'", file.getOriginalFilename(), e);
            throw new StorageException("Failed to read uploaded stream: " + e.getMessage(), e);
        }

        ValidatedFile scannedFile = virusScanService.scan(validatedFile);

        String storageKey = null;
        try {
            StorageResult storageResult = storageService.store(
                    scannedFile.content(),
                    scannedFile.sanitizedFilename(),
                    scannedFile.detectedMimeType(),
                    scannedFile.size()
            );
            storageKey = storageResult.key();

            String fileUrl = fileUrlResolver.toUrl(storageKey);

            return new StoredFileResult(
                    storageKey,
                    fileUrl,
                    scannedFile.sanitizedFilename(),
                    scannedFile.detectedMimeType(),
                    scannedFile.size()
            );
        } catch (Exception ex) {
            if (storageKey != null) {
                try {
                    storageService.delete(storageKey);
                } catch (Exception deleteEx) {
                    log.error("Compensating delete failed for storage key '{}'", storageKey, deleteEx);
                }
            }
            throw ex;
        }
    }
}
