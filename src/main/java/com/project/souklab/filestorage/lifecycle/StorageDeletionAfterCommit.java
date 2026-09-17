package com.project.souklab.filestorage.lifecycle;

import com.project.souklab.filestorage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;

/** Deletes one storage object after the surrounding transaction commits. */
@Slf4j
final class StorageDeletionAfterCommit implements TransactionSynchronization {

    private final StorageService storageService;
    private final String storageKey;

    StorageDeletionAfterCommit(StorageService storageService, String storageKey) {
        this.storageService = storageService;
        this.storageKey = storageKey;
    }

    @Override
    public void afterCommit() {
        try {
            storageService.delete(storageKey);
        } catch (Exception exception) {
            log.error("Failed to delete storage object '{}'", storageKey, exception);
        }
    }
}
