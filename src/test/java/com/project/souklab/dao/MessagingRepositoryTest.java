package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Conversation;
import com.project.souklab.model.ConversationParticipant;
import com.project.souklab.model.Message;
import com.project.souklab.model.MessageAttachment;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.search.enabled=false"
})
class MessagingRepositoryTest {
    private static final LocalDateTime MESSAGE_TIME = LocalDateTime.of(2026, 9, 17, 12, 0);

    @Autowired TestEntityManager entityManager;
    @Autowired ConversationRepository conversationRepository;
    @Autowired ConversationParticipantRepository conversationParticipantRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired MessageAttachmentRepository messageAttachmentRepository;

    @Test
    void conversationQueriesLoadParticipantsAndExcludeDeletedConversations() {
        User first = persistUser("first@chat.test");
        User second = persistUser("second@chat.test");
        Conversation active = persistConversation(first, second);
        Conversation deleted = persistConversation(first, second);
        deleted.setDeletedAt(MESSAGE_TIME);
        entityManager.flush();
        entityManager.clear();

        assertThat(conversationRepository.findBetween(first, second)).get()
                .extracting(Conversation::getId).isEqualTo(active.getId());
        assertThat(conversationRepository.findAllForUser(first)).singleElement()
                .satisfies(conversation -> assertThat(conversation.getParticipants()).hasSize(2));
    }

    @Test
    void messageQueriesProvideStableCursorOrderingAndAttachmentIsolation() {
        User owner = persistUser("owner@chat.test");
        User other = persistUser("other@chat.test");
        User outsider = persistUser("outsider@chat.test");
        Conversation conversation = persistConversation(owner, other);
        Message older = persistMessage(conversation, owner, "m1", "older");
        Message newer = persistMessage(conversation, other, "m2", "newer");
        Message deleted = persistMessage(conversation, other, "m3", "deleted");
        deleted.setDeletedAt(MESSAGE_TIME);
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(newer);
        attachment.setStorageKey("private-key");
        attachment.setOriginalFilename("private.txt");
        attachment.setContentType("text/plain");
        attachment.setSize(7);
        entityManager.persist(attachment);
        entityManager.flush();
        entityManager.getEntityManager().createQuery("update Message m set m.createdAt = :time")
                .setParameter("time", MESSAGE_TIME)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        assertThat(messageRepository.findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(
                conversation, PageRequest.of(0, 10)).getContent())
                .extracting(Message::getId).containsExactly("m2", "m1");
        assertThat(messageRepository.findBefore(conversation, MESSAGE_TIME, "m2", PageRequest.of(0, 10)).getContent())
                .extracting(Message::getId).containsExactly("m1");
        assertThat(messageAttachmentRepository.findAccessibleByStorageKey("private-key", owner)).isPresent();
        assertThat(messageAttachmentRepository.findAccessibleByStorageKey("private-key", outsider)).isEmpty();
    }

    @Test
    void softDeletedParticipantCannotAccessConversationOrAttachments() {
        User removedUser = persistUser("removed@chat.test");
        User remainingUser = persistUser("remaining@chat.test");
        Conversation conversation = persistConversation(removedUser, remainingUser);
        ConversationParticipant removedParticipant = conversationParticipantRepository
                .findByConversationAndUser(conversation, removedUser).orElseThrow();
        removedParticipant.setDeletedAt(MESSAGE_TIME);
        Message message = persistMessage(conversation, remainingUser, "removed-member-message", "message");
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(message);
        attachment.setStorageKey("removed-member-key");
        attachment.setOriginalFilename("file.txt");
        attachment.setContentType("text/plain");
        attachment.setSize(1);
        entityManager.persist(attachment);
        entityManager.flush();
        entityManager.clear();

        Conversation reloaded = entityManager.find(Conversation.class, conversation.getId());
        User reloadedUser = entityManager.find(User.class, removedUser.getId());
        assertThat(conversationParticipantRepository.findByConversationAndUser(reloaded, reloadedUser)).isEmpty();
        assertThat(conversationParticipantRepository.existsByConversationAndUser(reloaded, reloadedUser)).isFalse();
        assertThat(messageAttachmentRepository.findAccessibleByStorageKey("removed-member-key", reloadedUser)).isEmpty();
    }

    private User persistUser(String email) {
        return entityManager.persist(User.builder().email(email).status(AccountStatus.ACTIVE).build());
    }

    private Conversation persistConversation(User first, User second) {
        Conversation conversation = entityManager.persist(new Conversation());
        ConversationParticipant firstParticipant = new ConversationParticipant();
        firstParticipant.setConversation(conversation);
        firstParticipant.setUser(first);
        ConversationParticipant secondParticipant = new ConversationParticipant();
        secondParticipant.setConversation(conversation);
        secondParticipant.setUser(second);
        entityManager.persist(firstParticipant);
        entityManager.persist(secondParticipant);
        return conversation;
    }

    private Message persistMessage(Conversation conversation, User author, String id, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversation(conversation);
        message.setAuthor(author);
        message.setContent(content);
        message.setIdempotencyKey(id + "-key");
        message.setCreatedAt(MESSAGE_TIME);
        return entityManager.persist(message);
    }
}
