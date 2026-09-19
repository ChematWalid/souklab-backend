package com.project.souklab.dao;
import java.time.LocalDateTime;


import com.project.souklab.model.ContentReport;
import com.project.souklab.model.ReportStatus;
import com.project.souklab.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence operations for user-submitted content reports.
 */
public interface ContentReportRepository extends JpaRepository<ContentReport, String> {
    long countByStatus(ReportStatus status);
    long countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(ReportStatus status,
                                                            LocalDateTime from, LocalDateTime to);
    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);
    Page<ContentReport> findByStatus(ReportStatus status, Pageable pageable);
    Page<ContentReport> findByTargetType(ReportTargetType targetType, Pageable pageable);
    Page<ContentReport> findByTargetTypeAndStatus(ReportTargetType targetType, ReportStatus status, Pageable pageable);
}
