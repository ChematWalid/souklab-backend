package com.project.souklab.service.auth;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.*;
import com.project.souklab.dto.auth.*;
import com.project.souklab.dto.profile.ProfileResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.*;
import com.project.souklab.security.JwtUtils;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.profile.ProfileResponseMapper;
import com.project.souklab.service.security.RefreshTokenService;
import com.project.souklab.service.security.VerificationTokenService;
import com.project.souklab.util.EmailUtil;
import com.project.souklab.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication and identity service.
 * Owns credentials, token issuance/rotation, account lockout, email verification,
 * password management, and Google OAuth2 flow.
 * <p>
 * Profile lifecycle (getCurrentUser, completeProfile, patchCurrentUser) lives in
 * {@link com.project.souklab.service.profile.ProfileService}.
 * Mapping logic lives in {@link ProfileResponseMapper}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_ARTISAN_NAME = "ROLE_ARTISAN";
    private static final String ROLE_CLIENT_NAME = "ROLE_CLIENT";
    private static final String ERROR_USER_NOT_FOUND_PREFIX = "User not found: ";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthIdentityRepository oauthIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;
    private final VerificationTokenService verificationTokenService;
    private final EmailUtil emailUtil;
    private final AuditLogService auditLogService;
    private final Clock clock;
    private final ProfileResponseMapper profileResponseMapper;

    /**
     * Registers a new user.
     * ARTISAN users start with status PENDING (requiring administrative review).
     * CLIENT users start with status ACTIVE (can immediately log in and participate).
     * Public registration strictly prohibits ADMIN accounts.
     *
     * @param dto the registration payload containing email, password, role, and name fields
     * @return the profile response for the newly created user
     * @throws ConflictException         if the email is already registered
     * @throws BadRequestException       if the requested role is invalid or ADMIN
     * @throws ResourceNotFoundException if the resolved role does not exist in the database
     */
    @Transactional
    public ProfileResponse registerUser(UserRegistrationDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        Role assignedRole = validateRegistrationRole(dto.getRole());
        boolean isArtisan = assignedRole.getName().equals(ROLE_ARTISAN_NAME);
        AccountStatus initialStatus = isArtisan ? AccountStatus.PENDING : AccountStatus.ACTIVE;

        User savedUser = userRepository.save(buildNewUser(dto, email, assignedRole, initialStatus));

        try {
            String rawCode = verificationTokenService.issueToken(savedUser, VerificationTokenType.EMAIL_VERIFICATION);
            emailUtil.sendVerificationCode(savedUser.getEmail(), rawCode);
        } catch (Exception e) {
            log.warn("Could not issue or send verification code to {}: {}", savedUser.getEmail(), e.getMessage());
        }

        if (isArtisan) {
            try {
                notificationService.notifyAdmins("New artisan registration pending approval: " + savedUser.getEmail());
            } catch (Exception e) {
                log.warn("Could not dispatch admin notification for registration: {}", e.getMessage());
            }
        }

        return profileResponseMapper.mapToProfileResponse(savedUser);
    }

    /**
     * Authenticates a user by email and password, issuing access + refresh token pair.
     * Enforces a 15-minute temporary lockout after 5 consecutive failed login attempts.
     *
     * @param dto     the login credentials (identifier + password)
     * @param request the incoming HTTP request, used to capture the client IP address
     * @return a JWT response containing access token, refresh token, and user summary
     * @throws BadRequestException   if the login identifier is blank
     * @throws UnauthorizedException if credentials are invalid or the account uses social login
     * @throws ForbiddenException    if the account is temporarily locked, suspended, or rejected
     */
    @Transactional(noRollbackFor = {UnauthorizedException.class, BadRequestException.class, ForbiddenException.class})
    public JwtResponseDTO login(LoginDTO dto, HttpServletRequest request) {
        String identifier = dto.getLoginIdentifier();
        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Email is required for login.");
        }

        String email = identifier.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UnauthorizedException("This account was created via social login. Please sign in with Google.");
        }

        verifyAccountNotLocked(user);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
        }

        resolveExpiredSuspension(user);

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now(clock));
        if (request != null) {
            user.setLastLoginIp(extractClientIp(request));
        }
        userRepository.save(user);

        return generateJwtResponse(user);
    }

    /**
     * Rotates refresh tokens (revokes old, issues new pair).
     *
     * @param request the token refresh payload containing the current refresh token string
     * @return a new JWT response with rotated access and refresh tokens
     * @throws UnauthorizedException if the provided refresh token is not found or already revoked
     */
    @Transactional
    public JwtResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        RefreshToken newToken = refreshTokenService.rotateRefreshToken(oldToken);
        User user = newToken.getUser();

        String accessToken = jwtUtils.generateAccessToken(user.getEmail());

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(newToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(profileResponseMapper.mapToProfileResponse(user))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    /**
     * Revokes all refresh tokens for the given user.
     *
     * @param userEmail       the email address of the user whose tokens should be revoked, or {@code null}
     * @param refreshTokenStr a specific refresh token to revoke immediately, or {@code null}
     */
    @Transactional
    public void logout(String userEmail, String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshTokenStr.trim());
        }
        if (userEmail != null && !userEmail.isBlank()) {
            userRepository.findByEmail(userEmail.toLowerCase())
                    .ifPresent(refreshTokenService::deleteByUser);
        }
    }

    /**
     * Verifies a user's email address using a submitted 6-digit verification code.
     *
     * @param dto the verification payload containing email and the 6-digit code
     * @throws ResourceNotFoundException if no user exists with the provided email
     * @throws BadRequestException       if the verification code is invalid or expired
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public void verifyEmail(VerifyEmailRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.getEmail()));

        verificationTokenService.validateAndConsume(user, VerificationTokenType.EMAIL_VERIFICATION, dto.getCode());

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now(clock));
        userRepository.save(user);

        auditLogService.logAction(AuditLogAction.EMAIL_VERIFIED, "Email verified for user: " + user.getEmail(), user.getEmail());
    }

    /**
     * Resends an email verification code if the user exists and is not yet verified.
     * Always produces identical outward response behaviour to prevent account enumeration.
     *
     * @param dto the resend payload containing the user's email address
     */
    @Transactional
    public void resendVerification(ResendVerificationRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                try {
                    String rawCode = verificationTokenService.issueToken(user, VerificationTokenType.EMAIL_VERIFICATION);
                    emailUtil.sendVerificationCode(user.getEmail(), rawCode);
                } catch (Exception e) {
                    log.warn("Failed to issue or send resend verification email for {}: {}", email, e.getMessage());
                }
            }
        });
    }

    /**
     * Initiates the password reset flow.
     * If the user exists with a password, issues a reset token and emails the 6-digit code.
     * If the user exists without a password (OAuth-only), emails an informational notice.
     * Always returns a generic success response outward.
     *
     * @param dto the forgot-password payload containing the user's email address
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                try {
                    String rawCode = verificationTokenService.issueToken(user, VerificationTokenType.PASSWORD_RESET);
                    emailUtil.sendPasswordResetCode(user.getEmail(), rawCode);
                } catch (Exception e) {
                    log.warn("Failed to issue or send password reset code for {}: {}", email, e.getMessage());
                }
            } else {
                try {
                    emailUtil.sendOAuthOnlyPasswordResetNotice(user.getEmail());
                } catch (Exception e) {
                    log.warn("Failed to send OAuth password reset notice for {}: {}", email, e.getMessage());
                }
            }
        });
    }

    /**
     * Resets a user's password using the submitted 6-digit code.
     * Invalidates all active refresh tokens for the user upon completion.
     *
     * @param dto the reset-password payload containing email, code, and new password
     * @throws BadRequestException if the code is invalid or expired
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public void resetPassword(ResetPasswordRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code."));

        verificationTokenService.validateAndConsume(user, VerificationTokenType.PASSWORD_RESET, dto.getCode());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        auditLogService.logAction(AuditLogAction.PASSWORD_RESET_COMPLETED, "Password reset completed for user: " + user.getEmail(), user.getEmail());
    }

    /**
     * Changes an authenticated user's password.
     * <ol>
     *   <li>Resolves current user from SecurityContext.</li>
     *   <li>Rejects OAuth-only users (no password to change).</li>
     *   <li>Verifies oldPassword matches current password.</li>
     *   <li>Validates newPassword is not identical to oldPassword.</li>
     *   <li>Encodes and saves new password.</li>
     *   <li>Deletes active refresh tokens to force re-authentication across devices.</li>
     *   <li>Sends password-changed email notification.</li>
     *   <li>Logs PASSWORD_CHANGED audit log entry on success.</li>
     * </ol>
     *
     * @param request the change-password payload containing old and new passwords
     * @throws UnauthorizedException     if no user is authenticated, or the old password is incorrect
     * @throws BadRequestException       if the account uses social login and has no password
     * @throws ResourceNotFoundException if the authenticated email does not map to an existing user
     */
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + email));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("This account was created via social login and does not have a password to change. Please continue signing in with Google.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        try {
            emailUtil.sendPasswordChangedNotice(user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send password changed notice email for {}: {}", user.getEmail(), e.getMessage());
        }

        auditLogService.logAction(AuditLogAction.PASSWORD_CHANGED, "Password changed for user: " + user.getEmail(), user.getEmail());
    }

    /**
     * Processes Google OAuth2 authentication callback:
     * <ol>
     *   <li>Matches existing OAuthIdentity (provider=GOOGLE, provider_user_id)</li>
     *   <li>Otherwise matches existing User by verified email and auto-links</li>
     *   <li>Otherwise creates new User + OAuthIdentity with requested role from intent</li>
     * </ol>
     *
     * @param oAuth2User the principal returned by the OAuth2 provider
     * @param intentRole the role string captured during OAuth2 registration intent (e.g., "ARTISAN")
     * @param request    the incoming HTTP request used to capture the client IP address
     * @return a JWT response for the authenticated or newly created user
     * @throws BadRequestException       if the provider did not return an email, or the intent role is invalid
     * @throws ResourceNotFoundException if the resolved role does not exist in the database
     */
    @Transactional
    public JwtResponseDTO processOAuth2Success(OAuth2User oAuth2User, String intentRole, HttpServletRequest request) {
        String provider = "GOOGLE";
        String providerUserId = oAuth2User.getAttribute("sub");
        if (providerUserId == null || providerUserId.isBlank()) {
            providerUserId = oAuth2User.getName();
        }

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new BadRequestException("OAuth provider did not return an email address.");
        }
        email = email.trim().toLowerCase();

        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        var existingIdentity = oauthIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);

        User user;
        if (existingIdentity.isPresent()) {
            user = existingIdentity.get().getUser();
        } else {
            user = linkOrAuthenticateExistingIdentity(email, provider, providerUserId, firstName, lastName, picture, intentRole);
        }

        user.setLastLoginAt(LocalDateTime.now(clock));
        if (request != null) {
            user.setLastLoginIp(extractClientIp(request));
        }
        userRepository.save(user);

        return generateJwtResponse(user);
    }

    /**
     * Produces a role-specific login user summary embedded in the JWT response.
     *
     * @param user the authenticated user entity
     * @return a role-specific {@link ProfileResponse} suitable for embedding in the JWT response body
     */
    public ProfileResponse mapToLoginSummary(User user) {
        return profileResponseMapper.mapToProfileResponse(user);
    }

    /**
     * Builds a JWT response DTO from the given user.
     *
     * @param user the authenticated user entity
     * @return the assembled {@link JwtResponseDTO}
     */
    private JwtResponseDTO generateJwtResponse(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(user);

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(profileResponseMapper.mapToProfileResponse(user))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    /**
     * Validates the registration role input string and returns the matching {@link Role} entity.
     * Accepts bare role names (e.g., {@code "ARTISAN"}) or prefixed names (e.g., {@code "ROLE_ARTISAN"}).
     * ADMIN registration via the public endpoint is always rejected.
     *
     * @param roleInput the raw role string from the registration request, may be {@code null}
     * @return the resolved {@link Role} entity
     * @throws BadRequestException       if the role is ADMIN or not one of ARTISAN / CLIENT
     * @throws ResourceNotFoundException if the role does not exist in the database
     */
    private Role validateRegistrationRole(String roleInput) {
        String normalized = roleInput != null ? roleInput.trim().toUpperCase() : "";
        if (normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN")) {
            throw new BadRequestException("Administrator registration is not permitted via public registration.");
        }

        String roleName = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
        if (!roleName.equals(ROLE_ARTISAN_NAME) && !roleName.equals(ROLE_CLIENT_NAME)) {
            throw new BadRequestException("Invalid registration role. Allowed roles are ARTISAN or CLIENT.");
        }

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    /**
     * Constructs a new {@link User} entity from the registration DTO, resolved email, role, and initial status.
     * Falls back to splitting the {@code name} field when {@code firstName} is not provided.
     *
     * @param dto           the registration payload
     * @param email         the normalised (lowercase, trimmed) email address
     * @param assignedRole  the role entity to assign to the new user
     * @param initialStatus the initial {@link AccountStatus} derived from the role
     * @return a fully constructed but not yet persisted {@link User} entity
     */
    private User buildNewUser(UserRegistrationDTO dto, String email, Role assignedRole, AccountStatus initialStatus) {
        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();
        if ((firstName == null || firstName.isBlank()) && dto.getName() != null && !dto.getName().isBlank()) {
            String[] parts = dto.getName().trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(firstName)
                .lastName(lastName)
                .status(initialStatus)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(assignedRole)))
                .build();
    }

    /**
     * Throws {@link ForbiddenException} if the user's account is currently within
     * its temporary lockout window due to repeated failed login attempts.
     *
     * @param user the user entity to check
     * @throws ForbiddenException if the account is locked and the lockout period has not elapsed
     */
    private void verifyAccountNotLocked(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now(clock))) {
            throw new ForbiddenException("Too many failed login attempts. Account is temporarily locked. Please try again later.");
        }
    }

    /**
     * Increments the user's failed login attempt counter, applies a 15-minute lockout
     * when the threshold reaches five, persists the updated state, and throws
     * {@link UnauthorizedException} to terminate the login flow.
     *
     * @param user the user entity whose counter should be incremented
     * @throws UnauthorizedException always, to signal the password mismatch to the caller
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            user.setLockedUntil(LocalDateTime.now(clock).plusMinutes(15));
        }
        userRepository.save(user);
        throw new UnauthorizedException("Invalid email or password.");
    }

    /**
     * Lifts a temporary suspension if its end date has passed.
     * If the suspension is still active, throws {@link ForbiddenException}.
     * Accounts with status {@link AccountStatus#REJECTED} always throw.
     *
     * @param user the user entity to evaluate
     * @throws ForbiddenException if the account is actively suspended or was rejected
     */
    private void resolveExpiredSuspension(User user) {
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            if (!user.isSuspensionActive(LocalDateTime.now(clock))) {
                user.setStatus(AccountStatus.ACTIVE);
                user.setBannedUntil(null);
                user.setBanReason(null);
            } else {
                throw new ForbiddenException("Account is suspended: " + (user.getBanReason() != null ? user.getBanReason() : "Please contact support."));
            }
        }

        if (user.getStatus() == AccountStatus.REJECTED) {
            throw new ForbiddenException("Account registration was rejected: " + (user.getBanReason() != null ? user.getBanReason() : "Please contact support."));
        }
    }

    /**
     * Resolves or creates the {@link User} entity for a Google OAuth2 flow
     * where no existing {@link OAuthIdentity} was found for the provider user ID.
     * <ol>
     *   <li>If a user with the same email already exists, auto-links a new {@link OAuthIdentity} to them.</li>
     *   <li>Otherwise, creates a new user and identity using the registration intent role.</li>
     * </ol>
     *
     * @param email           the normalised OAuth2 email address
     * @param provider        the OAuth2 provider name (e.g., "GOOGLE")
     * @param providerUserId  the provider-specific user identifier
     * @param firstName       the given name returned by the provider
     * @param lastName        the family name returned by the provider
     * @param picture         the avatar URL returned by the provider
     * @param intentRole      the role intent string set at the start of the OAuth2 flow (e.g., "ARTISAN")
     * @return the resolved or newly created {@link User} entity
     * @throws BadRequestException       if the intent role is missing or invalid
     * @throws ResourceNotFoundException if the resolved role does not exist in the database
     */
    private User linkOrAuthenticateExistingIdentity(String email, String provider, String providerUserId,
                                                    String firstName, String lastName, String picture,
                                                    String intentRole) {
        var existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            return autoLinkByVerifiedEmail(existingUserByEmail.get(), provider, providerUserId, email);
        }

        return createOAuthUserAndProfile(email, provider, providerUserId, firstName, lastName, picture, intentRole);
    }

    /**
     * Links a new {@link OAuthIdentity} to an existing {@link User} found by verified email.
     * This covers the case where the user previously registered with a password and is now
     * signing in with Google for the first time.
     *
     * @param user           the existing user to link the identity to
     * @param provider       the OAuth2 provider name
     * @param providerUserId the provider-specific user identifier
     * @param email          the normalised email address
     * @return the same {@link User} instance with the new identity persisted
     */
    private User autoLinkByVerifiedEmail(User user, String provider, String providerUserId, String email) {
        OAuthIdentity identity = OAuthIdentity.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .build();
        oauthIdentityRepository.save(identity);
        return user;
    }

    /**
     * Creates a brand-new {@link User} and {@link OAuthIdentity} for a first-time OAuth2 registrant.
     * Derives the account status and role from the intent string captured at registration start.
     *
     * @param email          the normalised email address
     * @param provider       the OAuth2 provider name
     * @param providerUserId the provider-specific user identifier
     * @param firstName      the given name returned by the provider
     * @param lastName       the family name returned by the provider
     * @param picture        the avatar URL returned by the provider
     * @param intentRole     the role intent string (e.g., "ARTISAN" or "CLIENT")
     * @return the persisted {@link User} entity
     * @throws BadRequestException       if the intent role is blank or does not match ARTISAN or CLIENT
     * @throws ResourceNotFoundException if the resolved role does not exist in the database
     */
    private User createOAuthUserAndProfile(String email, String provider, String providerUserId,
                                           String firstName, String lastName, String picture,
                                           String intentRole) {
        if (intentRole == null || intentRole.isBlank()) {
            throw new BadRequestException("OAuth registration intent not found or expired. Please initiate registration from the artisan or client signup page.");
        }

        String normalizedIntent = intentRole.trim().toUpperCase();
        String roleName;
        AccountStatus initialStatus;
        if (normalizedIntent.contains("ARTISAN")) {
            roleName = ROLE_ARTISAN_NAME;
            initialStatus = AccountStatus.PENDING;
        } else if (normalizedIntent.contains("CLIENT")) {
            roleName = ROLE_CLIENT_NAME;
            initialStatus = AccountStatus.ACTIVE;
        } else {
            throw new BadRequestException("Invalid OAuth registration role intent: " + intentRole);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .email(email)
                .password(null)
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(picture)
                .status(initialStatus)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now(clock))
                .roles(new HashSet<>(Set.of(role)))
                .build();

        user = userRepository.save(user);

        OAuthIdentity identity = OAuthIdentity.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .build();
        oauthIdentityRepository.save(identity);

        return user;
    }

    /**
     * Extracts the originating client IP address from the request.
     * Prefers the first entry in the {@code X-Forwarded-For} header when present,
     * falling back to the direct {@code remoteAddr}.
     *
     * @param request the HTTP servlet request
     * @return the resolved IP address string
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
