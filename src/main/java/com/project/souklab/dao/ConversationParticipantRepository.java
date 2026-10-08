package com.project.souklab.dao;

import com.project.souklab.model.Conversation;
import com.project.souklab.model.ConversationParticipant;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, String> {
    @Query("select p from ConversationParticipant p where p.conversation = :conversation and p.user = :user and p.deletedAt is null")
    Optional<ConversationParticipant> findByConversationAndUser(@Param("conversation") Conversation conversation, @Param("user") User user);

    @Query("select case when count(p) > 0 then true else false end from ConversationParticipant p where p.conversation = :conversation and p.user = :user and p.deletedAt is null")
    boolean existsByConversationAndUser(@Param("conversation") Conversation conversation, @Param("user") User user);

    @Query("select distinct p2.user.email from ConversationParticipant p1, ConversationParticipant p2 " +
           "where p1.conversation = p2.conversation " +
           "and p1.user.email = :email and p2.user.email != :email " +
           "and p1.deletedAt is null and p2.deletedAt is null and p1.conversation.deletedAt is null")
    List<String> findPartnerEmailsByUserEmail(@Param("email") String email);
}
