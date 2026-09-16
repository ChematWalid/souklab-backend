package com.project.souklab.dto.report;

import com.project.souklab.model.ReportResolutionAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Administrator action for resolving a content report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResolutionRequestDTO {
    @NotNull
    private ReportResolutionAction action;
    @NotBlank
    @Size(max = 2000)
    private String note;
}
