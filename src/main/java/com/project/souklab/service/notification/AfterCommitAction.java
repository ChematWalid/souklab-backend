package com.project.souklab.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;

/** Executes a notification action only after the surrounding transaction commits. */
@RequiredArgsConstructor
public final class AfterCommitAction implements TransactionSynchronization {

    private final Runnable action;

    @Override
    public void afterCommit() {
        action.run();
    }
}
