package com.project.souklab.dto.report;

import com.project.souklab.model.ContentReport;
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
    String targetType;
    String targetId;
    String reason;
    String details;
    String status;
    String resolutionAction;
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
                .targetType(report.getTargetType().name())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus().name())
                .resolutionAction(report.getResolutionAction() == null ? null : report.getResolutionAction().name())
                .resolverId(report.getResolver() == null ? null : report.getResolver().getId())
                .resolutionNote(report.getResolutionNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
