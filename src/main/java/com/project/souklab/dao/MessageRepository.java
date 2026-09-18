package com.project.souklab.dao;

import com.project.souklab.model.Conversation;
import com.project.souklab.model.Message;
import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;

public interface MessageRepository extends JpaRepository<Message, String> {
    @EntityGraph(attributePaths = {"author", "attachments"})
    @Query("select m from Message m where m.conversation = :conversation and m.deletedAt is null order by m.createdAt desc, m.id desc")
    Page<Message> findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("conversation") Conversation conversation, Pageable pageable);
    @EntityGraph(attributePaths = {"author", "attachments"})
    @Query("select m from Message m where m.conversation = :conversation order by m.createdAt desc, m.id desc")
    Page<Message> findByConversationOrderByCreatedAtDesc(@Param("conversation") Conversation conversation, Pageable pageable);
    @EntityGraph(attributePaths = {"author", "attachments"})
    Page<Message> findByConversationAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(Conversation conversation, LocalDateTime before, Pageable pageable);
    @EntityGraph(attributePaths = {"author", "attachments"})
    @Query("select m from Message m join fetch m.author where m.conversation = :conversation and (m.createdAt < :beforeTime or (m.createdAt = :beforeTime and m.id < :beforeId)) order by m.createdAt desc, m.id desc")
    Page<Message> findBefore(@Param("conversation") Conversation conversation, @Param("beforeTime") LocalDateTime beforeTime, @Param("beforeId") String beforeId, Pageable pageable);
    @EntityGraph(attributePaths = {"author", "attachments"})
    Optional<Message> findByIdAndConversationAndDeletedAtIsNull(String id, Conversation conversation);
    Optional<Message> findByConversationAndAuthorAndIdempotencyKey(Conversation conversation, User author, String idempotencyKey);

    @Query("select count(m) from Message m where m.conversation = :conversation and m.author <> :reader and m.deletedAt is null and (:afterTime is null or m.createdAt > :afterTime)")
    long countUnread(@Param("conversation") Conversation conversation, @Param("reader") User reader, @Param("afterTime") LocalDateTime afterTime);
}
