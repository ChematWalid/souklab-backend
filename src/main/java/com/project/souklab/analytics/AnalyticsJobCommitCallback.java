package com.project.souklab.analytics;

import org.springframework.transaction.support.TransactionSynchronization;

/** Dispatches an analytics job only after its submission transaction commits. */
public class AnalyticsJobCommitCallback implements TransactionSynchronization {
    private final String jobId;
    private final AnalyticsJobService service;

    public AnalyticsJobCommitCallback(String jobId, AnalyticsJobService service) {
        this.jobId = jobId;
        this.service = service;
    }

    @Override
    public void afterCommit() {
        service.processAsync(jobId);
    }
}
