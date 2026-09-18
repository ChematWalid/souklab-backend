package com.project.souklab.dao;

import com.project.souklab.model.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.souklab.model.User;
import java.util.Optional;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {
    @Query("select a from MessageAttachment a join a.message m join m.conversation c join c.participants p where a.storageKey = :key and p.user = :user and p.deletedAt is null and a.deletedAt is null and m.deletedAt is null and c.deletedAt is null")
    Optional<MessageAttachment> findAccessibleByStorageKey(@Param("key") String key, @Param("user") User user);
}
