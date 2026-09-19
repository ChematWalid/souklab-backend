package com.project.souklab.analytics;

import com.project.souklab.config.AnalyticsProperties;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.dto.analytics.AnalyticsRebuildResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.model.analytics.ActivityEvent;
import com.project.souklab.model.analytics.DailyKpiRollup;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class AnalyticsRebuildService {
    private final ActivityEventRepository events;
    private final DailyKpiRollupRepository rollups;
    private final AnalyticsProperties properties;
    private final AuditLogService auditLogService;

    @Transactional
    public AnalyticsRebuildResponse rebuild(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) throw new BadRequestException("Invalid rebuild date range");
        if (to.toEpochDay() - from.toEpochDay() + 1 > properties.getMaximumRangeDays()) {
            throw new BadRequestException("Rebuild range exceeds configured maximum");
        }
        ZoneId businessZone = ZoneId.of(properties.getBusinessTimeZone());
        LocalDateTime start = from.atStartOfDay(businessZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime end = to.plusDays(1).atStartOfDay(businessZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        rollups.deleteAll(rollups.findByRollupDateBetween(from, to));
        Map<String, Long> counts = new HashMap<>();
        int pageNumber = 0;
        int sourceCount = 0;
        List<ActivityEvent> source;
        do {
            source = events.findByEventTimeBetweenOrderByEventTimeAsc(start, end.minusNanos(1),
                    PageRequest.of(pageNumber++, properties.getRollupBatchSize()));
            sourceCount += source.size();
            for (ActivityEvent event : source) {
                LocalDate eventDay = event.getEventTime().toInstant(ZoneOffset.UTC)
                        .atZone(businessZone).toLocalDate();
                String key = eventDay + "\u0000event." + event.getEventType().value();
                counts.merge(key, 1L, Long::sum);
            }
        } while (source.size() == properties.getRollupBatchSize());
        int written = 0;
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            String[] parts = entry.getKey().split("\u0000", 2);
            DailyKpiRollup rollup = new DailyKpiRollup();
            rollup.setRollupDate(LocalDate.parse(parts[0])); rollup.setKpiKey(parts[1]);
            rollup.setValue(entry.getValue()); rollup.setSourceVersion(1);
            rollups.save(rollup); written++;
        }
        auditLogService.logAction(AuditLogAction.ANALYTICS_REBUILD,
                "range=" + from + ".." + to + ", events=" + sourceCount + ", rollups=" + written);
        return new AnalyticsRebuildResponse(from, to, sourceCount, written);
    }
}
