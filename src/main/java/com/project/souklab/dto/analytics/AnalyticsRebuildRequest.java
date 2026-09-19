package com.project.souklab.dto.analytics;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnalyticsRebuildRequest(@NotNull LocalDate fromDate, @NotNull LocalDate toDate) { }
