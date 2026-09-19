package com.project.souklab.analytics;

import com.project.souklab.config.AnalyticsProperties;
import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.ContentReportRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.dto.analytics.AnalyticsRebuildResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.analytics.DailyKpiRollup;
import com.project.souklab.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reconstructs only KPIs supported by historical source timestamps. */
@Service
@RequiredArgsConstructor
public class AnalyticsBackfillService {
    private final AnalyticsProperties properties;
    private final DailyKpiRollupRepository rollups;
    private final UserRepository users;
    private final FeedPostRepository feedPosts;
    private final FormationRepository formations;
    private final FormationEnrollmentRepository enrollments;
    private final ArtisanReviewRepository reviews;
    private final ContentReportRepository reports;
    private final PaymentRepository payments;
    private final AuditLogService auditLogService;
    private final EntityManager entityManager;

    @Transactional
    public AnalyticsRebuildResponse backfill(LocalDate from, LocalDate to) {
        validateRange(from, to);
        rollups.deleteAll(rollups.findByRollupDateBetween(from, to));
        int written = 0;
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            LocalDateTime start = utcStart(day);
            LocalDateTime end = utcStart(day.plusDays(1));
            LocalDateTime inclusiveEnd = end.minusNanos(1);
            Map<String, Long> metrics = new LinkedHashMap<>();
            metrics.put("historical.registrations", users.countByCreatedAtBetween(start, inclusiveEnd));
            metrics.put("historical.feed_posts", feedPosts.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            metrics.put("historical.formations", formations.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            metrics.put("historical.enrollments", enrollments.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            metrics.put("historical.reviews", reviews.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            metrics.put("historical.reports", reports.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            metrics.put("historical.payments", payments.countByCreatedAtBetweenAndDeletedAtIsNull(start, inclusiveEnd));
            for (Map.Entry<String, Long> metric : metrics.entrySet()) {
                if (metric.getValue() == 0) continue;
                DailyKpiRollup rollup = new DailyKpiRollup();
                rollup.setRollupDate(day);
                rollup.setKpiKey(metric.getKey());
                rollup.setValue(metric.getValue());
                rollup.setSourceVersion(2);
                rollups.save(rollup);
                written++;
                if (written % properties.getBackfillBatchSize() == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }
        entityManager.flush();
        auditLogService.logAction(AuditLogAction.ANALYTICS_REBUILD,
                "historical-backfill range=" + from + ".." + to + ", rollups=" + written);
        return new AnalyticsRebuildResponse(from, to, 0, written);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BadRequestException("Invalid backfill date range");
        }
        if (to.toEpochDay() - from.toEpochDay() + 1 > properties.getMaximumRangeDays()) {
            throw new BadRequestException("Backfill range exceeds configured maximum");
        }
    }

    private LocalDateTime utcStart(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(properties.getBusinessTimeZone()))
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
