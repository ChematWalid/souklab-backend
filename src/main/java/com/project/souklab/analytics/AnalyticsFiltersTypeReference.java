package com.project.souklab.analytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.souklab.model.analytics.AnalyticsFilterKey;

import java.util.Map;

/** Named Jackson type token for persisted analytics filters. */
public final class AnalyticsFiltersTypeReference extends TypeReference<Map<AnalyticsFilterKey.Event, String>> {
}
