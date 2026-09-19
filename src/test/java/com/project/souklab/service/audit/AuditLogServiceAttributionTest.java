package com.project.souklab.service.audit;

import com.project.souklab.dao.AuditLogRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AuditLog;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceAttributionTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("2-arg logAction resolves authenticated user on calling thread and links user entity")
    void testLogAction_whenAuthenticatedUser_attributesLogToUser() {
        String adminEmail = "admin@souklab.com";
        User adminUser = User.builder()
                .email(adminEmail)
                .build();
        adminUser.setId("admin-uuid-123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(adminEmail);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        auditLogService.logAction(AuditLogAction.User.APPROVED, "Approved user ID: 123");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditLogAction.User.APPROVED);
        assertThat(saved.getDetails()).isEqualTo("Approved user ID: 123");
        assertThat(saved.getUser()).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("2-arg logAction falls back to anonymous attribution when no authenticated user in context")
    void testLogAction_whenUnauthenticated_savesLogWithoutUser() {
        SecurityContextHolder.clearContext();

        auditLogService.logAction(AuditLogAction.User.BANNED, "Banned user ID: 456");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditLogAction.User.BANNED);
        assertThat(saved.getDetails()).isEqualTo("Banned user ID: 456");
        assertThat(saved.getUser()).isNull();
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("3-arg logAction persists with the supplied username attribution")
    void testLogActionWithUsername_persistsAuditEntry() {
        auditLogService.logAction(AuditLogAction.Permission.Grant.VALUE, "Granted permission", "admin@souklab.com");

        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditLogAction.Permission.Grant.VALUE
                        && "Granted permission".equals(log.getDetails())));
        verify(userRepository).findByEmail("admin@souklab.com");
    }

    @Test
    void explicitAnonymousUsernameDoesNotResolveAUser() {
        auditLogService.logAction(AuditLogAction.User.BANNED, "anonymous action", "anonymousUser");
        verify(auditLogRepository).save(any(AuditLog.class));
        verify(userRepository, never()).findByEmail("anonymousUser");
    }

    @Test
    @DisplayName("audit persistence failures are contained by the audit service")
    void testLogAction_whenPersistenceFails_doesNotPropagateException() {
        doThrow(new IllegalStateException("database unavailable"))
                .when(auditLogRepository).save(any(AuditLog.class));

        auditLogService.logAction(AuditLogAction.User.BANNED, "Ban failed");

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
