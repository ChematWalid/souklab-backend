package com.project.souklab.dao;

import com.project.souklab.model.MessageAttachmentUpload;
import com.project.souklab.model.User;
import com.project.souklab.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MessageAttachmentUploadRepository extends JpaRepository<MessageAttachmentUpload, String> {
    @Query("select u from MessageAttachmentUpload u where u.storageKey = :storageKey and u.owner = :owner and u.conversation = :conversation and u.usedAt is null and u.deletedAt is null")
    Optional<MessageAttachmentUpload> findByStorageKeyAndOwnerAndConversationAndUsedAtIsNull(
            @Param("storageKey") String storageKey,
            @Param("owner") User owner,
            @Param("conversation") Conversation conversation);
}
