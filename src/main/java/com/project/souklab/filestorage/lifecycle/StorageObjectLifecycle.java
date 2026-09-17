package com.project.souklab.filestorage.lifecycle;

import com.project.souklab.filestorage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Coordinates deletion of storage objects with database transaction completion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StorageObjectLifecycle {

    private final StorageService storageService;

    /**
     * Deletes an object after the current transaction commits, or immediately when no synchronization is active.
     *
     * @param storageKey opaque storage key to delete
     */
    public void deleteAfterCommit(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new StorageDeletionAfterCommit(storageService, storageKey));
        } else {
            delete(storageKey);
        }
    }

    private void delete(String storageKey) {
        try {
            storageService.delete(storageKey);
        } catch (Exception exception) {
            log.error("Failed to delete storage object '{}'", storageKey, exception);
        }
    }
}
