package com.project.souklab.dto.analytics;

import java.time.LocalDate;

public record AnalyticsRebuildResponse(LocalDate fromDate, LocalDate toDate, int eventsRead, int rollupsWritten) { }
