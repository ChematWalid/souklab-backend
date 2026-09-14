package com.project.souklab.dto.formation;

import com.project.souklab.filestorage.controller.FileServingController;
import com.project.souklab.model.FormationFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a downloadable attachment or syllabus file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationFileResponseDTO {

    /**
     * Unique identifier of the formation file record.
     */
    private String id;

    /**
     * Original filename as uploaded by the instructor.
     */
    private String originalFilename;

    /**
     * MIME media content type of the file.
     */
    private String contentType;

    /**
     * Physical payload size in bytes.
     */
    private long fileSize;

    /**
     * Authenticated endpoint URL to download or stream the course file.
     */
    private String downloadUrl;

    /**
     * Timestamp when the course file was uploaded.
     */
    private LocalDateTime createdAt;

    /**
     * Factory method mapping a FormationFile entity to a FormationFileResponseDTO with a configurable route prefix.
     *
     * @param file the formation file entity
     * @param fileServingPrefix configured file-serving route prefix
     * @return populated FormationFileResponseDTO
     */
    public static FormationFileResponseDTO from(FormationFile file, String fileServingPrefix) {
        if (file == null) {
            return null;
        }
        String prefix = (fileServingPrefix != null && !fileServingPrefix.isBlank())
                ? (fileServingPrefix.endsWith("/") ? fileServingPrefix : fileServingPrefix + "/")
                : FileServingController.DEFAULT_FILE_SERVING_PREFIX;
        return FormationFileResponseDTO.builder()
                .id(file.getId())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .downloadUrl(prefix + file.getStorageKey())
                .createdAt(file.getCreatedAt())
                .build();
    }

    /**
     * Factory method mapping a FormationFile entity to a FormationFileResponseDTO using default route prefix.
     *
     * @param file the formation file entity
     * @return populated FormationFileResponseDTO
     */
    public static FormationFileResponseDTO from(FormationFile file) {
        return from(file, FileServingController.DEFAULT_FILE_SERVING_PREFIX);
    }
}
