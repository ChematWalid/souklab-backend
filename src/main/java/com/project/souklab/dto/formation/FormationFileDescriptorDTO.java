package com.project.souklab.dto.formation;

import com.project.souklab.model.FormationFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object describing a downloadable course file attachment within public catalog views.
 * Download link is active only for confirmed enrolled participants and the authoring instructor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationFileDescriptorDTO {

    /**
     * Unique identifier of the formation file.
     */
    private String id;

    /**
     * Original filename of the uploaded attachment.
     */
    private String filename;

    /**
     * MIME media type of the attachment (e.g. application/pdf, image/png).
     */
    private String contentType;

    /**
     * File payload size in bytes.
     */
    private long fileSize;

    /**
     * Protected download URI, populated only if caller is enrolled or authoring instructor.
     */
    private String downloadUrl;

    /**
     * Factory method mapping a FormationFile entity to a FormationFileDescriptorDTO.
     *
     * @param file the formation file entity
     * @param downloadUrl accessible download URL or null if access restricted
     * @return populated FormationFileDescriptorDTO
     */
    public static FormationFileDescriptorDTO from(FormationFile file, String downloadUrl) {
        if (file == null) {
            return null;
        }
        return FormationFileDescriptorDTO.builder()
                .id(file.getId())
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .downloadUrl(downloadUrl)
                .build();
    }
}
