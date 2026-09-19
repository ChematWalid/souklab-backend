package com.project.souklab.dto.report;

import com.project.souklab.model.ContentReport;
import com.project.souklab.model.ReportResolutionAction;
import com.project.souklab.model.ReportStatus;
import com.project.souklab.model.ReportTargetType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Administrator representation of a content report.
 */
@Value
@Builder
public class ContentReportResponseDTO {
    String id;
    String reporterId;
    ReportTargetType targetType;
    String targetId;
    String reason;
    String details;
    ReportStatus status;
    ReportResolutionAction resolutionAction;
    String resolverId;
    String resolutionNote;
    LocalDateTime createdAt;

    /**
     * Maps an entity to a response.
     *
     * @param report report entity
     * @return response DTO
     */
    public static ContentReportResponseDTO from(ContentReport report) {
        return ContentReportResponseDTO.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .resolutionAction(report.getResolutionAction())
                .resolverId(report.getResolver() == null ? null : report.getResolver().getId())
                .resolutionNote(report.getResolutionNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
