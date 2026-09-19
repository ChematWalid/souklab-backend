package com.project.souklab.service.user;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.analytics.AnalyticsMetadata;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.auth.UserResponseDTO;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.security.Permission;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.souklab.config.AppProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final String ERROR_USER_NOT_FOUND_PREFIX = "User not found with id: ";

    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final Clock clock;
    private final AppProperties appProperties;
    private ActivityEventService activityEventService;

    @Autowired(required = false)
    void setActivityEventService(ActivityEventService value) { this.activityEventService = value; }

    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param search optional search query for filtering by email or name
     * @param pageable the pagination parameters specifying page size, number, and sorting
     * @return a paginated response containing a list of all users as UserResponseDTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> getAllUsers(String search, Pageable pageable) {
        Page<User> page;
        if (search != null && !search.isBlank()) {
            page = userRepository.searchUsers(search, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return PaginatedResponse.from(page.map(this::mapToDTO));
    }

    /**
     * Retrieves a paginated list of users whose registrations are currently pending approval.
     * Typically used by admins to vet new artisan accounts before activating them.
     *
     * @param pageable the pagination parameters
     * @return a paginated response containing the list of pending UserResponseDTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> getPendingUsers(Pageable pageable) {
        Page<UserResponseDTO> page = userRepository.findByStatus(AccountStatus.PENDING, pageable)
                .map(this::mapToDTO);
        return PaginatedResponse.from(page);
    }

    /**
     * Approves a user's pending registration, transitioning their status from PENDING to ACTIVE.
     * Logs the action and triggers an approval notification to the user.
     *
     * @param userId the unique identifier of the user to approve
     * @throws ResourceNotFoundException if the user is not found
     * @throws BadRequestException if the user is not in a PENDING status
     */
    @Transactional
    public void approveUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + userId));

        if (user.getStatus() != AccountStatus.PENDING) {
            throw new BadRequestException("User is not pending approval. Current status: " + user.getStatus());
        }

        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        artisanRepository.findById(userId).ifPresent(artisan -> {
            artisan.setVerified(true);
            artisanRepository.save(artisan);
        });

        auditLogService.logAction(AuditLogAction.APPROVE_USER, "Approved user ID: " + userId);
        recordModeration(AnalyticsEvent.User.APPROVED, user, Map.of());
        notificationService.createForUser(user, "Your account has been approved and is now active!", NotificationType.Account.VALIDATED, user.getId());
    }

    /**
     * Permanently or indefinitely bans a user from the platform.
     * Immediately revokes active refresh tokens for the user to force complete logout.
     *
     * @param userId the unique identifier of the user to ban
     * @param reason the administrative justification for the ban
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public void banUser(String userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + userId));

        user.setStatus(AccountStatus.SUSPENDED);
        user.setBanReason(reason != null ? reason : appProperties.getAdmin().getDefaultBanReason());
        user.setBannedUntil(null);

        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        auditLogService.logAction(AuditLogAction.BAN_USER, "Banned user ID: " + userId + ". Reason: " + reason);
        recordModeration(AnalyticsEvent.User.SUSPENDED, user, Map.of(AnalyticsMetadata.Moderation.REASON_PRESENT, reason != null && !reason.isBlank()));
        notificationService.createForUser(user, "Your account has been permanently suspended. Reason: " + reason, NotificationType.Account.SUSPENDED, user.getId());
    }

    /**
     * Temporarily suspends (timeouts) a user for a designated duration in minutes.
     * Immediately revokes active refresh tokens to force the user to log out during the timeout.
     *
     * @param userId the unique identifier of the user to timeout
     * @param minutes the duration of the suspension in minutes
     * @param reason the administrative justification for the timeout
     * @throws ResourceNotFoundException if the user is not found
     * @throws BadRequestException if minutes is non-positive
     */
    @Transactional
    public void timeoutUser(String userId, int minutes, String reason) {
        if (minutes <= 0) {
            throw new BadRequestException("Timeout duration must be greater than 0 minutes");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + userId));

        user.setStatus(AccountStatus.SUSPENDED);
        user.setBanReason(reason != null ? reason : appProperties.getAdmin().getDefaultTimeoutReason());
        user.setBannedUntil(LocalDateTime.now(clock).plusMinutes(minutes));
        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        auditLogService.logAction(AuditLogAction.TIMEOUT_USER, "Timed out user ID: " + userId + " for " + minutes + " minutes. Reason: " + reason);
        recordModeration(AnalyticsEvent.User.TIMED_OUT, user, Map.of(AnalyticsMetadata.Moderation.MINUTES, minutes));
        notificationService.createForUser(user, "Your account has been timed out for " + minutes + " minutes. Reason: " + reason, NotificationType.Account.SUSPENDED, user.getId());
    }

    /**
     * Reinstates a suspended user, restoring their status to ACTIVE and clearing timeout restrictions.
     *
     * @param userId the unique identifier of the user to unban
     * @throws ResourceNotFoundException if the user is not found
     * @throws ConflictException if the user is not currently suspended
     */
    @Transactional
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + userId));

        if (user.getStatus() != AccountStatus.SUSPENDED) {
            throw new ConflictException("User is not suspended. Current status: " + user.getStatus());
        }

        user.setStatus(AccountStatus.ACTIVE);
        user.setBannedUntil(null);
        user.setBanReason(null);
        userRepository.save(user);

        auditLogService.logAction(AuditLogAction.UNBAN_USER, "Reinstated user ID: " + userId);
        recordModeration(AnalyticsEvent.User.REINSTATED, user, Map.of());
        notificationService.createForUser(user, "Your account suspension has been lifted and your access has been restored.", NotificationType.Account.REINSTATED, user.getId());
    }

    private void recordModeration(AnalyticsEvent.Type type, User user,
                                  Map<? extends AnalyticsMetadata.Key, ?> metadata) {
        if (activityEventService != null) activityEventService.record(type, user.getId(), user.getId(), metadata);
    }

    private UserResponseDTO mapToDTO(User user) {
        Set<Permission> roleNames = user.getPermissions().stream()
                .map(AuthorizationPermission::getPermissionKey)
                .map(Permission::fromValue)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        String name = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : "")).trim();
        if (name.isEmpty()) {
            name = user.getEmail();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        AccountStatus effectiveStatus = user.getEffectiveStatus(now);
        boolean isExpiredTimeout = user.getStatus() != effectiveStatus;

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(name)
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(effectiveStatus)
                .emailVerified(user.isEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .permissions(roleNames)
                .bannedUntil(isExpiredTimeout ? null : user.getBannedUntil())
                .banReason(isExpiredTimeout ? null : user.getBanReason())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
