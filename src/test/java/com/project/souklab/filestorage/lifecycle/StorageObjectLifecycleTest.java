package com.project.souklab.filestorage.lifecycle;

import com.project.souklab.filestorage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StorageObjectLifecycleTest {
    @Mock StorageService storage;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void ignoresBlankKeysAndDeletesImmediatelyWithoutTransaction() {
        StorageObjectLifecycle lifecycle = new StorageObjectLifecycle(storage);
        lifecycle.deleteAfterCommit(null);
        lifecycle.deleteAfterCommit(" ");
        lifecycle.deleteAfterCommit("key");
        verify(storage).delete("key");
        verify(storage, never()).delete(null);
    }

    @Test
    void defersDeletionUntilCommit() {
        TransactionSynchronizationManager.initSynchronization();
        StorageObjectLifecycle lifecycle = new StorageObjectLifecycle(storage);
        lifecycle.deleteAfterCommit("key");
        verify(storage, never()).delete("key");
        TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();
        verify(storage).delete("key");
    }

    @Test
    void swallowsStorageFailures() {
        doThrow(new IllegalStateException("storage unavailable")).when(storage).delete("key");
        new StorageObjectLifecycle(storage).deleteAfterCommit("key");
        verify(storage).delete("key");
    }

    @Test
    void deferredDeletionSwallowsStorageFailuresAfterCommit() {
        doThrow(new IllegalStateException("storage unavailable")).when(storage).delete("key");
        TransactionSynchronizationManager.initSynchronization();
        try {
            new StorageObjectLifecycle(storage).deleteAfterCommit("key");
            TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();
            verify(storage).delete("key");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
