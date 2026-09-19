package com.project.souklab.dao;

import java.util.List;

import com.project.souklab.model.Conversation;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @EntityGraph(attributePaths = {"participants", "participants.user"})
    @Query("select c from Conversation c join c.participants p1 join c.participants p2 where p1.user = :first and p2.user = :second and p1.deletedAt is null and p2.deletedAt is null and c.deletedAt is null")
    Optional<Conversation> findBetween(@Param("first") User first, @Param("second") User second);

    @EntityGraph(attributePaths = {"participants", "participants.user"})
    @Query("select c from Conversation c join c.participants p where p.user = :user and p.deletedAt is null and c.deletedAt is null order by c.updatedAt desc")
    List<Conversation> findAllForUser(@Param("user") User user);
}
