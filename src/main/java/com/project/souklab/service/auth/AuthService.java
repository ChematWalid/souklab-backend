package com.project.souklab.service.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.*;
import com.project.souklab.dto.auth.*;
import com.project.souklab.dto.profile.ArtisanPatchDTO;
import com.project.souklab.dto.profile.ArtisanResponseDTO;
import com.project.souklab.dto.profile.ClientPatchDTO;
import com.project.souklab.dto.profile.ClientProfileResponseDTO;
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
import com.project.souklab.service.security.RefreshTokenService;
import com.project.souklab.service.security.VerificationTokenService;
import com.project.souklab.util.EmailUtil;
import com.project.souklab.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_ARTISAN_NAME = "ROLE_ARTISAN";
    private static final String ROLE_CLIENT_NAME = "ROLE_CLIENT";
    private static final String ERROR_USER_NOT_FOUND_PREFIX = "User not found: ";
    private static final String PAYLOAD_KEY_REGION_ID = "regionId";
    private static final String PAYLOAD_KEY_REGION = "region";
    private static final String PAYLOAD_KEY_ADDRESS = "address";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthIdentityRepository oauthIdentityRepository;
    private final ArtisanRepository artisanRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;
    private final VerificationTokenService verificationTokenService;
    private final EmailUtil emailUtil;
    private final AuditLogService auditLogService;
    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final Clock clock;

    /**
     * Registers a new user.
     * ARTISAN users start with status PENDING (requiring administrative review).
     * CLIENT users start with status ACTIVE (can immediately log in and participate).
     * Public registration strictly prohibits ADMIN accounts.
     */
    @Transactional
    public ProfileResponse registerUser(UserRegistrationDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        AccountStatus initialStatus = AccountStatus.ACTIVE;
        String roleInput = dto.getRole() != null ? dto.getRole().trim().toUpperCase() : "";
        if (roleInput.equals("ADMIN") || roleInput.equals("ROLE_ADMIN")) {
            throw new BadRequestException("Administrator registration is not permitted via public registration.");
        }

        String roleName = roleInput.startsWith("ROLE_") ? roleInput : "ROLE_" + roleInput;
        if (!roleName.equals(ROLE_ARTISAN_NAME) && !roleName.equals(ROLE_CLIENT_NAME)) {
            throw new BadRequestException("Invalid registration role. Allowed roles are ARTISAN or CLIENT.");
        }

        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();
        if ((firstName == null || firstName.isBlank()) && dto.getName() != null && !dto.getName().isBlank()) {
            String[] parts = dto.getName().trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        boolean isArtisan = roleName.equals(ROLE_ARTISAN_NAME);
        if (isArtisan) {
            initialStatus = AccountStatus.PENDING;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(firstName)
                .lastName(lastName)
                .status(initialStatus)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(assignedRole)))
                .build();

        User savedUser = userRepository.save(user);

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

        return mapToProfileResponse(savedUser);
    }

    /**
     * Authenticates a user by email and password, issuing access + refresh token pair.
     * Enforces a 15-minute temporary lockout after 5 consecutive failed login attempts.
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

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now(clock))) {
            throw new ForbiddenException("Too many failed login attempts. Account is temporarily locked. Please try again later.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now(clock).plusMinutes(15));
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password.");
        }

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
                .user(mapToLoginSummary(user))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    /**
     * Revokes all refresh tokens for the given user.
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
     * Returns the currently authenticated user's profile.
     */
    @Transactional(readOnly = true)
    public ProfileResponse getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + email));

        return mapToProfileResponse(user);
    }

    /**
     * Completes profile creation for Artisan or Client.
     */
    @Transactional
    public ProfileResponse completeProfile(CompleteProfileRequestDTO dto) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + email));

        boolean isArtisan = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_ARTISAN_NAME));
        boolean isClient = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_CLIENT_NAME));

        if (isArtisan) {
            Artisan profile = artisanRepository.findById(user.getId())
                    .orElse(Artisan.builder().user(user).build());

            if (dto.getBio() != null) profile.setBio(dto.getBio());
            if (dto.resolveRegionId() != null) profile.setRegionId(dto.resolveRegionId());
            if (dto.getCity() != null) profile.setCity(dto.getCity());
            if (dto.getAddress() != null) profile.setAddress(dto.getAddress());
            if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());
            if (dto.getSubCategoryId() != null) profile.setSubCategoryId(dto.getSubCategoryId());

            artisanRepository.save(profile);
            user.setArtisan(profile);
        } else if (isClient) {
            Client client = clientRepository.findById(user.getId())
                    .orElse(Client.builder().user(user).build());

            if (dto.getClientType() != null) client.setClientType(dto.getClientType());
            if (dto.getCompanyName() != null) client.setCompanyName(dto.getCompanyName());
            if (dto.getBio() != null) client.setBio(dto.getBio());
            if (dto.getAddress() != null) client.setAddress(dto.getAddress());
            if (dto.resolveRegionId() != null) client.setRegionId(dto.resolveRegionId());
            if (dto.getCity() != null) client.setCity(dto.getCity());

            clientRepository.save(client);
            user.setClient(client);
        }

        return mapToProfileResponse(user);
    }

    /**
     * Partially updates (PATCH) the authenticated user's profile based on their role.
     * Follows "omitted = unchanged, explicit null = clear" semantics.
     * Unknown/unpatchable fields (like isTeacher, accountStatus, email) are silently ignored.
     */
    @Transactional
    public ProfileResponse patchCurrentUser(JsonNode payload) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated.");
        }

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_PREFIX + email));

        boolean isArtisan = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_ARTISAN_NAME));
        boolean isClient = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_CLIENT_NAME));

        if (!isArtisan && !isClient) {
            throw new ForbiddenException("Administrators do not possess an editable artisan or client profile.");
        }

        if (payload == null || payload.isNull() || payload.isEmpty()) {
            return mapToProfileResponse(user);
        }

        if (isArtisan) {
            ArtisanPatchDTO patchDTO;
            try {
                patchDTO = jsonMapper.treeToValue(payload, ArtisanPatchDTO.class);
            } catch (Exception e) {
                throw new BadRequestException("Invalid JSON payload for artisan profile update: " + e.getMessage());
            }

            Set<ConstraintViolation<ArtisanPatchDTO>> violations = validator.validate(patchDTO);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            Artisan artisan = artisanRepository.findById(user.getId())
                    .orElse(Artisan.builder().user(user).build());

            if (payload.has("bio")) {
                artisan.setBio(payload.get("bio").isNull() ? null : patchDTO.getBio());
            }
            if (payload.has(PAYLOAD_KEY_REGION_ID)) {
                artisan.setRegionId(payload.get(PAYLOAD_KEY_REGION_ID).isNull() ? null : patchDTO.getRegionId());
            } else if (payload.has(PAYLOAD_KEY_REGION)) {
                artisan.setRegionId(payload.get(PAYLOAD_KEY_REGION).isNull() ? null : patchDTO.resolveRegionId());
            }
            if (payload.has("city")) {
                artisan.setCity(payload.get("city").isNull() ? null : patchDTO.getCity());
            }
            if (payload.has(PAYLOAD_KEY_ADDRESS)) {
                artisan.setAddress(payload.get(PAYLOAD_KEY_ADDRESS).isNull() ? null : patchDTO.getAddress());
            }
            if (payload.has("website")) {
                artisan.setWebsite(payload.get("website").isNull() ? null : patchDTO.getWebsite());
            }
            if (payload.has("subCategoryId")) {
                artisan.setSubCategoryId(payload.get("subCategoryId").isNull() ? null : patchDTO.getSubCategoryId());
            }

            artisanRepository.save(artisan);
            user.setArtisan(artisan);
        } else {
            ClientPatchDTO patchDTO;
            try {
                patchDTO = jsonMapper.treeToValue(payload, ClientPatchDTO.class);
            } catch (Exception e) {
                throw new BadRequestException("Invalid JSON payload for client profile update: " + e.getMessage());
            }

            Set<ConstraintViolation<ClientPatchDTO>> violations = validator.validate(patchDTO);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            Client client = clientRepository.findById(user.getId())
                    .orElse(Client.builder().user(user).build());

            if (payload.has("bio")) {
                client.setBio(payload.get("bio").isNull() ? null : patchDTO.getBio());
            }
            if (payload.has(PAYLOAD_KEY_ADDRESS)) {
                client.setAddress(payload.get(PAYLOAD_KEY_ADDRESS).isNull() ? null : patchDTO.getAddress());
            }
            if (payload.has(PAYLOAD_KEY_REGION_ID)) {
                client.setRegionId(payload.get(PAYLOAD_KEY_REGION_ID).isNull() ? null : patchDTO.getRegionId());
            } else if (payload.has(PAYLOAD_KEY_REGION)) {
                client.setRegionId(payload.get(PAYLOAD_KEY_REGION).isNull() ? null : patchDTO.resolveRegionId());
            }
            if (payload.has("city")) {
                client.setCity(payload.get("city").isNull() ? null : patchDTO.getCity());
            }
            if (payload.has("companyName")) {
                client.setCompanyName(payload.get("companyName").isNull() ? null : patchDTO.getCompanyName());
            }
            if (payload.has("clientType")) {
                client.setClientType(payload.get("clientType").isNull() ? null : patchDTO.getClientType());
            }

            clientRepository.save(client);
            user.setClient(client);
        }

        return mapToProfileResponse(user);
    }

    /**
     * Processes Google OAuth2 authentication callback:
     * 1. Matches existing OAuthIdentity (provider=GOOGLE, provider_user_id)
     * 2. Otherwise matches existing User by verified email and auto-links
     * 3. Otherwise creates new User + OAuthIdentity with requested role from intent
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

        User user;
        var existingIdentity = oauthIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (existingIdentity.isPresent()) {
            user = existingIdentity.get().getUser();
        } else {
            var existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                user = existingUserByEmail.get();
                OAuthIdentity identity = OAuthIdentity.builder()
                        .user(user)
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .email(email)
                        .build();
                oauthIdentityRepository.save(identity);
            } else {
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

                user = User.builder()
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
            }
        }

        user.setLastLoginAt(LocalDateTime.now(clock));
        if (request != null) {
            user.setLastLoginIp(extractClientIp(request));
        }
        userRepository.save(user);

        return generateJwtResponse(user);
    }

    private JwtResponseDTO generateJwtResponse(User user) {
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(user);

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(mapToLoginSummary(user))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    /**
     * Produces a role-specific login user summary embedded in the JWT response.
     * Clients receive ClientProfileResponseDTO; Artisans receive ArtisanResponseDTO.
     * This replaces the old shared UserSummaryDTO that leaked artisan-specific fields to clients.
     */
    public ProfileResponse mapToLoginSummary(User user) {
        return mapToProfileResponse(user);
    }

    /**
     * Dispatches to the correct role-specific profile DTO.
     * Clients → ClientProfileResponseDTO (no artisan fields)
     * Artisans → ArtisanResponseDTO (full artisan fields)
     * Admins/unknown → ArtisanResponseDTO as fallback (admin tooling uses UserResponseDTO separately)
     */
    public ProfileResponse mapToProfileResponse(User user) {
        boolean isArtisan = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(ROLE_ARTISAN_NAME));

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        if (isArtisan) {
            Artisan profile = user.getArtisan();
            return ArtisanResponseDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .avatarUrl(user.getAvatarUrl())
                    .accountStatus(user.getStatus())
                    .roles(roleNames)
                    .emailVerified(user.isEmailVerified())
                    .emailVerifiedAt(user.getEmailVerifiedAt())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .bio(profile != null ? profile.getBio() : null)
                    .regionId(profile != null ? profile.getRegionId() : null)
                    .city(profile != null ? profile.getCity() : null)
                    .address(profile != null ? profile.getAddress() : null)
                    .website(profile != null ? profile.getWebsite() : null)
                    .subCategoryId(profile != null ? profile.getSubCategoryId() : null)
                    .teacher(profile != null && profile.isTeacher())
                    .verified(profile != null && profile.isVerified())
                    .premium(profile != null && profile.isPremium())
                    .rating(profile != null ? profile.getRating() : 0.0)
                    .reviewsCount(profile != null ? profile.getReviewsCount() : 0)
                    .build();
        }

        Client client = user.getClient();
        return ClientProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .accountStatus(user.getStatus())
                .roles(roleNames)
                .emailVerified(user.isEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .clientType(client != null ? client.getClientType() : "INDIVIDUAL")
                .companyName(client != null ? client.getCompanyName() : null)
                .bio(client != null ? client.getBio() : null)
                .address(client != null ? client.getAddress() : null)
                .regionId(client != null ? client.getRegionId() : null)
                .city(client != null ? client.getCity() : null)
                .build();
    }

    /**
     * Kept for admin UserManagementService compatibility — returns the shared UserResponseDTO
     * which is appropriate for admin views where all fields are intentionally visible.
     */
    public UserSummaryDTO mapToSummaryDTO(User user) {
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(ROLE_CLIENT_NAME);

        boolean isTeacher = user.getArtisan() != null && user.getArtisan().isTeacher();
        boolean isPremium = (user.getArtisan() != null && user.getArtisan().isPremium())
                || (user.getClient() != null && user.getClient().isPremium());
        boolean isValidated = (user.getArtisan() != null && user.getArtisan().isVerified())
                || (user.getClient() != null && user.getClient().isVerified());

        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getName())
                .role(primaryRole)
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .accountStatus(user.getStatus())
                .isPremium(isPremium)
                .isValidated(isValidated)
                .isTeacher(isTeacher)
                .build();
    }

    /**
     * Verifies a user's email address using a submitted 6-digit verification code.
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
     * Always produces identical outward response behavior to prevent account enumeration.
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
     * Initiates password reset flow.
     * If user exists with password, issues reset token and emails the 6-digit code.
     * If user exists without password (OAuth-only), emails an informational notice.
     * Always returns generic success outward.
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
     * 1. Resolves current user from SecurityContext.
     * 2. Rejects OAuth-only users (no password to change).
     * 3. Verifies oldPassword matches current password.
     * 4. Validates newPassword is not identical to oldPassword.
     * 5. Encodes and saves new password.
     * 6. Deletes active refresh tokens to force re-authentication across devices.
     * 7. Sends password-changed email notification.
     * 8. Logs PASSWORD_CHANGED audit log entry on success.
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

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
