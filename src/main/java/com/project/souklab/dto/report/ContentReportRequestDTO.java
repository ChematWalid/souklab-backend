package com.project.souklab.dto.report;

import com.project.souklab.model.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for reporting a user, feed post, or review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentReportRequestDTO {
    @NotNull
    private ReportTargetType targetType;
    @NotBlank
    @Size(max = 36)
    private String targetId;
    @NotBlank
    @Size(max = 100)
    private String reason;
    @Size(max = 5000)
    private String details;
}
