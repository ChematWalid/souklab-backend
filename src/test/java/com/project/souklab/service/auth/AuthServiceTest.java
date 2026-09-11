package com.project.souklab.service.auth;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ClientRepository;
import com.project.souklab.dao.OAuthIdentityRepository;
import com.project.souklab.dao.RefreshTokenRepository;
import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.auth.ChangePasswordRequestDTO;
import com.project.souklab.dto.auth.CompleteProfileRequestDTO;
import com.project.souklab.dto.auth.ForgotPasswordRequestDTO;
import com.project.souklab.dto.auth.JwtResponseDTO;
import com.project.souklab.dto.auth.LoginDTO;
import com.project.souklab.dto.auth.ResendVerificationRequestDTO;
import com.project.souklab.dto.auth.ResetPasswordRequestDTO;
import com.project.souklab.dto.auth.TokenRefreshRequestDTO;
import com.project.souklab.dto.auth.UserRegistrationDTO;
import com.project.souklab.dto.auth.UserSummaryDTO;
import com.project.souklab.dto.auth.VerifyEmailRequestDTO;
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
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.Client;
import com.project.souklab.model.OAuthIdentity;
import com.project.souklab.model.RefreshToken;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.model.VerificationTokenType;
import com.project.souklab.security.JwtUtils;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.security.RefreshTokenService;
import com.project.souklab.service.security.VerificationTokenService;
import com.project.souklab.util.EmailUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit test suite for AuthService covering every method and logical branch.
 * Enforces strict mocking with constructor injection and deterministic fixed clock.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private OAuthIdentityRepository oauthIdentityRepository;

    @Mock
    private ArtisanRepository artisanRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private Validator validator;

    private AppProperties appProperties;
    private Clock fixedClock;
    private LocalDateTime fixedNow;
    private AuthService authService;
    private Role artisanRole;
    private Role clientRole;

    /**
     * Initializes test fixtures, fixed clock at 2026-09-04T12:00:00Z, and AuthService instance.
     */
    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-04T12:00:00Z"), ZoneId.of("UTC"));
        fixedNow = LocalDateTime.now(fixedClock);

        appProperties = new AppProperties();
        appProperties.getJwt().setAccessTokenExpirationMs(900000L);
        appProperties.getJwt().setRefreshTokenExpirationMs(604800000L);

        artisanRole = new Role();
        artisanRole.setName("ROLE_ARTISAN");
        artisanRole.setDescription("Artisan role");

        clientRole = new Role();
        clientRole.setName("ROLE_CLIENT");
        clientRole.setDescription("Client role");

        authService = new AuthService(
                userRepository,
                roleRepository,
                refreshTokenRepository,
                oauthIdentityRepository,
                artisanRepository,
                clientRepository,
                passwordEncoder,
                notificationService,
                jwtUtils,
                refreshTokenService,
                appProperties,
                verificationTokenService,
                emailUtil,
                auditLogService,
                jsonMapper,
                validator,
                fixedClock
        );
    }

    /**
     * Clears SecurityContextHolder to prevent cross-test authentication leakage.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies registerUser rejects an existing email with ConflictException.
     */
    @Test
    @DisplayName("registerUser: throws ConflictException when email is already registered")
    void registerUser_whenEmailAlreadyExists_throwsConflictException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("Existing@Example.COM")
                .password("password123")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email is already registered: existing@example.com");

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies registerUser rejects public admin registration with BadRequestException.
     */
    @Test
    @DisplayName("registerUser: throws BadRequestException when role is ADMIN")
    void registerUser_whenRoleIsAdmin_throwsBadRequestException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("admin@example.com")
                .password("password123")
                .role("ADMIN")
                .build();

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Administrator registration is not permitted via public registration.");
    }

    /**
     * Verifies registerUser rejects public role admin registration with BadRequestException.
     */
    @Test
    @DisplayName("registerUser: throws BadRequestException when role is ROLE_ADMIN")
    void registerUser_whenRoleIsRoleAdmin_throwsBadRequestException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("admin@example.com")
                .password("password123")
                .role("ROLE_ADMIN")
                .build();

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Administrator registration is not permitted via public registration.");
    }

    /**
     * Verifies registerUser rejects unsupported roles with BadRequestException.
     */
    @Test
    @DisplayName("registerUser: throws BadRequestException when role is invalid")
    void registerUser_whenRoleIsInvalid_throwsBadRequestException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("someone@example.com")
                .password("password123")
                .role("MODERATOR")
                .build();

        when(userRepository.existsByEmail("someone@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid registration role. Allowed roles are ARTISAN or CLIENT.");
    }

    /**
     * Verifies registerUser throws ResourceNotFoundException when role is not present in repository.
     */
    @Test
    @DisplayName("registerUser: throws ResourceNotFoundException when role not found in repository")
    void registerUser_whenRoleNotFoundInRepository_throwsResourceNotFoundException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("password123")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found: ROLE_CLIENT");
    }

    /**
     * Verifies successful client registration parses full name, sets ACTIVE status, and sends verification code.
     */
    @Test
    @DisplayName("registerUser: creates active client with split full name and issues verification code")
    void registerUser_withValidClientRoleAndFullName_createsActiveUserAndIssuesVerificationCode() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .name("Jane Doe")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });
        when(verificationTokenService.issueToken(any(User.class), eq(VerificationTokenType.EMAIL_VERIFICATION)))
                .thenReturn("123456");

        ProfileResponse response = authService.registerUser(dto);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getEmail()).isEqualTo("client@example.com");
        assertThat(clientProfile.getFirstName()).isEqualTo("Jane");
        assertThat(clientProfile.getLastName()).isEqualTo("Doe");
        assertThat(clientProfile.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(clientProfile.isEmailVerified()).isFalse();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
        assertThat(savedUser.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        verify(emailUtil).sendVerificationCode("client@example.com", "123456");
        verifyNoInteractions(notificationService);
    }

    /**
     * Verifies registerUser handles single-word name by setting empty string for last name.
     */
    @Test
    @DisplayName("registerUser: creates active client with empty last name when single name given")
    void registerUser_withValidClientRoleAndSingleName_createsActiveUserWithEmptyLastName() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .name("Jane")
                .role("ROLE_CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });

        ProfileResponse response = authService.registerUser(dto);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getFirstName()).isEqualTo("Jane");
        assertThat(clientProfile.getLastName()).isEmpty();
    }

    /**
     * Verifies registerUser preserves explicit first and last names without splitting full name.
     */
    @Test
    @DisplayName("registerUser: uses explicit first and last name when provided directly")
    void registerUser_withValidClientRoleAndFirstLastName_createsActiveUserWithoutSplitting() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .firstName("Alice")
                .lastName("Smith")
                .name("Ignored Full Name")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });

        ProfileResponse response = authService.registerUser(dto);

        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getFirstName()).isEqualTo("Alice");
        assertThat(clientProfile.getLastName()).isEqualTo("Smith");
    }

    /**
     * Verifies successful artisan registration sets PENDING status and notifies administrators.
     */
    @Test
    @DisplayName("registerUser: creates pending artisan and notifies administrators")
    void registerUser_withValidArtisanRole_createsPendingUserAndNotifiesAdmins() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("artisan@example.com")
                .password("rawPassword123")
                .firstName("Karim")
                .lastName("Najar")
                .role("ARTISAN")
                .build();

        when(userRepository.existsByEmail("artisan@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ARTISAN")).thenReturn(Optional.of(artisanRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("artisan-user-id");
            return user;
        });
        when(verificationTokenService.issueToken(any(User.class), eq(VerificationTokenType.EMAIL_VERIFICATION)))
                .thenReturn("654321");

        ProfileResponse response = authService.registerUser(dto);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        ArtisanResponseDTO artisanProfile = (ArtisanResponseDTO) response;
        assertThat(artisanProfile.getEmail()).isEqualTo("artisan@example.com");
        assertThat(artisanProfile.getAccountStatus()).isEqualTo(AccountStatus.PENDING);

        verify(notificationService).notifyAdmins("New artisan registration pending approval: artisan@example.com");
        verify(emailUtil).sendVerificationCode("artisan@example.com", "654321");
    }

    /**
     * Verifies registerUser catches verification dispatch exception and completes registration.
     */
    @Test
    @DisplayName("registerUser: catches verification dispatch exception and completes successfully")
    void registerUser_whenVerificationTokenFails_catchesExceptionAndCompletesRegistration() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .firstName("Sara")
                .lastName("Ben")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });
        doThrow(new RuntimeException("Mail server down"))
                .when(verificationTokenService).issueToken(any(User.class), eq(VerificationTokenType.EMAIL_VERIFICATION));

        ProfileResponse response = authService.registerUser(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("client@example.com");
    }

    /**
     * Verifies registerUser catches admin notification exception and completes registration.
     */
    @Test
    @DisplayName("registerUser: catches admin notification exception and completes successfully")
    void registerUser_whenAdminNotificationFails_catchesExceptionAndCompletesRegistration() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("artisan@example.com")
                .password("rawPassword123")
                .firstName("Mehdi")
                .lastName("Alami")
                .role("ARTISAN")
                .build();

        when(userRepository.existsByEmail("artisan@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ARTISAN")).thenReturn(Optional.of(artisanRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-artisan-1");
            return user;
        });
        doThrow(new RuntimeException("Notification bus unavailable"))
                .when(notificationService).notifyAdmins(anyString());

        ProfileResponse response = authService.registerUser(dto);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("artisan@example.com");
    }

    /**
     * Verifies login rejects null login identifier with BadRequestException.
     */
    @Test
    @DisplayName("login: throws BadRequestException when login identifier is null")
    void login_whenIdentifierNull_throwsBadRequestException() {
        LoginDTO dto = LoginDTO.builder()
                .email(null)
                .username(null)
                .password("password123")
                .build();

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is required for login.");
    }

    /**
     * Verifies login rejects blank login identifier with BadRequestException.
     */
    @Test
    @DisplayName("login: throws BadRequestException when login identifier is blank")
    void login_whenIdentifierBlank_throwsBadRequestException() {
        LoginDTO dto = LoginDTO.builder()
                .email("   ")
                .password("password123")
                .build();

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is required for login.");
    }

    /**
     * Verifies login throws UnauthorizedException when user email does not exist.
     */
    @Test
    @DisplayName("login: throws UnauthorizedException when user not found")
    void login_whenUserNotFound_throwsUnauthorizedException() {
        LoginDTO dto = LoginDTO.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    /**
     * Verifies login rejects account with null password (social login user) with UnauthorizedException.
     */
    @Test
    @DisplayName("login: throws UnauthorizedException when user has null password")
    void login_whenUserHasNullPassword_throwsUnauthorizedException() {
        LoginDTO dto = LoginDTO.builder()
                .email("oauth@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .email("oauth@example.com")
                .password(null)
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("This account was created via social login. Please sign in with Google.");
    }

    /**
     * Verifies login rejects account with blank password with UnauthorizedException.
     */
    @Test
    @DisplayName("login: throws UnauthorizedException when user has blank password")
    void login_whenUserHasBlankPassword_throwsUnauthorizedException() {
        LoginDTO dto = LoginDTO.builder()
                .email("oauth@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .email("oauth@example.com")
                .password("   ")
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("This account was created via social login. Please sign in with Google.");
    }

    /**
     * Verifies login throws BadRequestException when account lockout timestamp is in the future.
     */
    @Test
    @DisplayName("login: throws ForbiddenException when account is temporarily locked")
    void login_whenAccountTemporarilyLocked_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("locked@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .email("locked@example.com")
                .password("hashedPassword")
                .lockedUntil(fixedNow.plusMinutes(10))
                .build();

        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Too many failed login attempts. Account is temporarily locked. Please try again later.");
    }

    /**
     * Verifies login proceeds when lockedUntil timestamp is in the past.
     */
    @Test
    @DisplayName("login: allows login when account lockout timestamp is in the past")
    void login_whenAccountLockedInPast_allowsLogin() {
        LoginDTO dto = LoginDTO.builder()
                .email("unlocked@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("unlocked@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .lockedUntil(fixedNow.minusMinutes(1))
                .failedLoginAttempts(5)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("unlocked@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("unlocked@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        JwtResponseDTO response = authService.login(dto, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    /**
     * Verifies login increments failed attempts and throws UnauthorizedException on wrong password.
     */
    @Test
    @DisplayName("login: increments failed attempts on first wrong password")
    void login_whenPasswordIncorrectFirstTime_incrementsFailedAttemptsAndThrowsUnauthorizedException() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
    }

    /**
     * Verifies login locks account for 15 minutes on fifth consecutive failed attempt.
     */
    @Test
    @DisplayName("login: locks account for 15 minutes on fifth consecutive failed attempt")
    void login_whenPasswordIncorrectFifthTime_locksAccountFor15MinutesAndThrowsUnauthorizedException() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .failedLoginAttempts(4)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isEqualTo(fixedNow.plusMinutes(15));
        verify(userRepository).save(user);
    }

    /**
     * Verifies login throws ForbiddenException with custom reason when account status is SUSPENDED.
     */
    @Test
    @DisplayName("login: throws ForbiddenException with ban reason when account is suspended")
    void login_whenAccountStatusSuspendedWithReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("suspended@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("suspended@example.com")
                .password("hashedPassword")
                .status(AccountStatus.SUSPENDED)
                .banReason("Terms of service breach")
                .build();

        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account is suspended: Terms of service breach");
    }

    /**
     * Verifies login throws ForbiddenException with default message when suspended user has no reason.
     */
    @Test
    @DisplayName("login: throws ForbiddenException with fallback message when suspended without reason")
    void login_whenAccountStatusSuspendedWithoutReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("suspended@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("suspended@example.com")
                .password("hashedPassword")
                .status(AccountStatus.SUSPENDED)
                .banReason(null)
                .build();

        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account is suspended: Please contact support.");
    }

    /**
     * Verifies login throws ForbiddenException when bannedUntil timestamp is in the future.
     */
    @Test
    @DisplayName("login: throws ForbiddenException when bannedUntil is in future")
    void login_whenAccountBannedUntilFutureWithReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("banned@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("banned@example.com")
                .password("hashedPassword")
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(fixedNow.plusDays(2))
                .banReason("Suspicious login activity")
                .build();

        when(userRepository.findByEmail("banned@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account is suspended: Suspicious login activity");
    }

    /**
     * Verifies login throws ForbiddenException with fallback when bannedUntil is in future with no reason.
     */
    @Test
    @DisplayName("login: throws ForbiddenException with fallback when bannedUntil in future without reason")
    void login_whenAccountBannedUntilFutureWithoutReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("banned@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("banned@example.com")
                .password("hashedPassword")
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(fixedNow.plusDays(2))
                .banReason(null)
                .build();

        when(userRepository.findByEmail("banned@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account is suspended: Please contact support.");
    }

    /**
     * Verifies login allows sign-in when account is SUSPENDED and bannedUntil timestamp is in the past,
     * lazily auto-reinstating the user to ACTIVE and clearing timeout fields.
     */
    @Test
    @DisplayName("login: allows sign-in when bannedUntil is in the past and auto-reinstates account")
    void login_whenAccountBannedUntilInPast_allowsLogin() {
        LoginDTO dto = LoginDTO.builder()
                .email("unbanned@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("unbanned@example.com")
                .password("hashedPassword")
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(fixedNow.minusDays(1))
                .banReason("Prior timeout")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("unbanned@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("unbanned@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        JwtResponseDTO response = authService.login(dto, null);

        assertThat(response).isNotNull();
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getBannedUntil()).isNull();
        assertThat(user.getBanReason()).isNull();
        assertThat(user.getLastLoginAt()).isEqualTo(fixedNow);
        verify(userRepository).save(user);
    }

    /**
     * Verifies login throws ForbiddenException when account status is REJECTED with reason.
     */
    @Test
    @DisplayName("login: throws ForbiddenException when registration rejected with reason")
    void login_whenAccountRegistrationRejectedWithReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("rejected@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("rejected@example.com")
                .password("hashedPassword")
                .status(AccountStatus.REJECTED)
                .banReason("Invalid craftsmanship portfolio")
                .build();

        when(userRepository.findByEmail("rejected@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account registration was rejected: Invalid craftsmanship portfolio");
    }

    /**
     * Verifies login throws ForbiddenException with fallback when account status is REJECTED without reason.
     */
    @Test
    @DisplayName("login: throws ForbiddenException with fallback when registration rejected without reason")
    void login_whenAccountRegistrationRejectedWithoutReason_throwsForbiddenException() {
        LoginDTO dto = LoginDTO.builder()
                .email("rejected@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("rejected@example.com")
                .password("hashedPassword")
                .status(AccountStatus.REJECTED)
                .banReason(null)
                .build();

        when(userRepository.findByEmail("rejected@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Account registration was rejected: Please contact support.");
    }

    /**
     * Verifies valid login resets failed attempts, clears lockout, updates login timestamp and issues tokens.
     */
    @Test
    @DisplayName("login: successful login with null request resets failed attempts and returns tokens")
    void login_withValidCredentialsAndNullRequest_resetsFailedAttemptsAndReturnsJwtResponse() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(3)
                .lockedUntil(null)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("user@example.com")).thenReturn("access-token-jwt");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token-uuid").build());

        JwtResponseDTO response = authService.login(dto, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        assertThat(response.getRoles()).containsExactly("ROLE_CLIENT");

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getLastLoginAt()).isEqualTo(fixedNow);
        assertThat(user.getLastLoginIp()).isNull();
        verify(userRepository).save(user);
    }

    /**
     * Verifies login parses first client IP from X-Forwarded-For header.
     */
    @Test
    @DisplayName("login: extracts client IP from X-Forwarded-For header")
    void login_withValidCredentialsAndXForwardedForHeader_recordsClientIp() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        authService.login(dto, request);

        assertThat(user.getLastLoginIp()).isEqualTo("203.0.113.195");
    }

    /**
     * Verifies login falls back to getRemoteAddr when X-Forwarded-For header is absent.
     */
    @Test
    @DisplayName("login: extracts remote address when X-Forwarded-For is absent")
    void login_withValidCredentialsAndRemoteAddr_recordsRemoteAddr() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        authService.login(dto, request);

        assertThat(user.getLastLoginIp()).isEqualTo("192.168.1.100");
    }

    /**
     * Verifies login supports username field when email field is absent in LoginDTO.
     */
    @Test
    @DisplayName("login: resolves identifier from username field when email is null")
    void login_withUsernameIdentifier_findsUserByEmail() {
        LoginDTO dto = LoginDTO.builder()
                .email(null)
                .username("artisan@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("artisan@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("artisan@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        JwtResponseDTO response = authService.login(dto, null);

        assertThat(response.getRoles()).containsExactly("ROLE_ARTISAN");
    }

    /**
     * Verifies refreshToken throws UnauthorizedException when token does not exist in repository.
     */
    @Test
    @DisplayName("refreshToken: throws UnauthorizedException when refresh token not found")
    void refreshToken_whenTokenNotFound_throwsUnauthorizedException() {
        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO();
        request.setRefreshToken("unknown-refresh-token");

        when(refreshTokenRepository.findByToken("unknown-refresh-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid refresh token.");
    }

    /**
     * Verifies refreshToken rotates valid token and returns new token pair.
     */
    @Test
    @DisplayName("refreshToken: rotates valid token and returns new JWT response")
    void refreshToken_whenTokenValid_rotatesTokenAndReturnsNewJwtResponse() {
        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO();
        request.setRefreshToken("old-refresh-token");

        User user = User.builder()
                .email("user@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        RefreshToken oldToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(user)
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .token("new-refresh-token")
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.rotateRefreshToken(oldToken)).thenReturn(newToken);
        when(jwtUtils.generateAccessToken("user@example.com")).thenReturn("new-access-token");

        JwtResponseDTO response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
    }

    /**
     * Verifies logout deletes specified refresh token and deletes all user tokens by email.
     */
    @Test
    @DisplayName("logout: deletes token and deletes user refresh tokens when both provided")
    void logout_withTokenAndEmail_deletesTokenAndUserRefreshTokens() {
        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        authService.logout("user@example.com", "token-to-revoke");

        verify(refreshTokenRepository).deleteByToken("token-to-revoke");
        verify(refreshTokenService).deleteByUser(user);
    }

    /**
     * Verifies logout executes safely without exceptions when both arguments are null or blank.
     */
    @Test
    @DisplayName("logout: handles null and blank arguments without errors")
    void logout_withNullAndBlankArguments_executesSafelyWithoutInteractingWithRepositories() {
        authService.logout(null, "");
        authService.logout("   ", null);

        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(refreshTokenService);
    }

    /**
     * Verifies logout deletes token only when email argument is null.
     */
    @Test
    @DisplayName("logout: deletes token only when email is null")
    void logout_withTokenOnly_deletesTokenWithoutUserLookup() {
        authService.logout(null, "token-only-xyz");

        verify(refreshTokenRepository).deleteByToken("token-only-xyz");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(refreshTokenService);
    }

    /**
     * Verifies logout deletes user tokens only when token argument is null.
     */
    @Test
    @DisplayName("logout: deletes user refresh tokens only when token argument is null")
    void logout_withEmailOnly_deletesUserRefreshTokensWithoutTokenLookup() {
        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        authService.logout("user@example.com", null);

        verifyNoInteractions(refreshTokenRepository);
        verify(refreshTokenService).deleteByUser(user);
    }

    /**
     * Verifies logout does not throw when email is not found in repository.
     */
    @Test
    @DisplayName("logout: does not throw when email not found in repository")
    void logout_whenEmailNotFoundInRepository_doesNotThrow() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        authService.logout("ghost@example.com", "token-123");

        verify(refreshTokenRepository).deleteByToken("token-123");
        verifyNoInteractions(refreshTokenService);
    }

    /**
     * Verifies getCurrentUser throws UnauthorizedException when unauthenticated.
     */
    @Test
    @DisplayName("getCurrentUser: throws UnauthorizedException when unauthenticated")
    void getCurrentUser_whenUnauthenticated_throwsUnauthorizedException() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Not authenticated.");
    }

    /**
     * Verifies getCurrentUser throws ResourceNotFoundException when authenticated username is not in DB.
     */
    @Test
    @DisplayName("getCurrentUser: throws ResourceNotFoundException when authenticated user not in DB")
    void getCurrentUser_whenUserNotFoundInRepository_throwsResourceNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@example.com", "cred", List.of())
        );

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@example.com");
    }

    /**
     * Verifies getCurrentUser returns ArtisanResponseDTO for authenticated artisan.
     */
    @Test
    @DisplayName("getCurrentUser: returns ArtisanResponseDTO for authenticated artisan")
    void getCurrentUser_withAuthenticatedArtisan_returnsArtisanProfileResponse() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        Artisan artisan = Artisan.builder()
                .bio("Master woodworker")
                .city("Fes")
                .rating(4.8)
                .reviewsCount(25)
                .isTeacher(true)
                .isVerified(true)
                .isPremium(true)
                .build();

        User user = User.builder()
                .email("artisan@example.com")
                .firstName("Karim")
                .lastName("Najar")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(artisanRole)))
                .artisan(artisan)
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = authService.getCurrentUser();

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        ArtisanResponseDTO profile = (ArtisanResponseDTO) response;
        assertThat(profile.getEmail()).isEqualTo("artisan@example.com");
        assertThat(profile.getBio()).isEqualTo("Master woodworker");
        assertThat(profile.getCity()).isEqualTo("Fes");
        assertThat(profile.isTeacher()).isTrue();
        assertThat(profile.isVerified()).isTrue();
        assertThat(profile.isPremium()).isTrue();
        assertThat(profile.getRating()).isEqualTo(4.8);
    }

    /**
     * Verifies getCurrentUser returns ClientProfileResponseDTO for authenticated client.
     */
    @Test
    @DisplayName("getCurrentUser: returns ClientProfileResponseDTO for authenticated client")
    void getCurrentUser_withAuthenticatedClient_returnsClientProfileResponse() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Client client = Client.builder()
                .companyName("Atlas Trade")
                .clientType("BUSINESS")
                .city("Casablanca")
                .build();

        User user = User.builder()
                .email("client@example.com")
                .firstName("Sara")
                .lastName("Mansour")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .client(client)
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = authService.getCurrentUser();

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ClientProfileResponseDTO profile = (ClientProfileResponseDTO) response;
        assertThat(profile.getEmail()).isEqualTo("client@example.com");
        assertThat(profile.getCompanyName()).isEqualTo("Atlas Trade");
        assertThat(profile.getClientType()).isEqualTo("BUSINESS");
    }

    /**
     * Verifies completeProfile throws UnauthorizedException when unauthenticated.
     */
    @Test
    @DisplayName("completeProfile: throws UnauthorizedException when unauthenticated")
    void completeProfile_whenUnauthenticated_throwsUnauthorizedException() {
        SecurityContextHolder.clearContext();
        CompleteProfileRequestDTO dto = new CompleteProfileRequestDTO();

        assertThatThrownBy(() -> authService.completeProfile(dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Not authenticated.");
    }

    /**
     * Verifies completeProfile throws ResourceNotFoundException when user is not found.
     */
    @Test
    @DisplayName("completeProfile: throws ResourceNotFoundException when user not found")
    void completeProfile_whenUserNotFound_throwsResourceNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@example.com", "cred", List.of())
        );
        CompleteProfileRequestDTO dto = new CompleteProfileRequestDTO();

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.completeProfile(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@example.com");
    }

    /**
     * Verifies completeProfile updates existing artisan entity without overwriting isTeacher.
     */
    @Test
    @DisplayName("completeProfile: updates existing artisan fields while preserving isTeacher")
    void completeProfile_forExistingArtisan_updatesAllFieldsAndReturnsProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-1");

        Artisan existingArtisan = Artisan.builder()
                .user(user)
                .isTeacher(false)
                .build();

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder()
                .bio("Updated bio")
                .regionId("REG-10")
                .city("Marrakech")
                .address("Souk Semmarine")
                .website("https://example.com/artisan")
                .subCategoryId("SUBCAT-1")
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(artisanRepository.findById("artisan-id-1")).thenReturn(Optional.of(existingArtisan));

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        verify(artisanRepository).save(existingArtisan);
        assertThat(existingArtisan.getBio()).isEqualTo("Updated bio");
        assertThat(existingArtisan.getRegionId()).isEqualTo("REG-10");
        assertThat(existingArtisan.getCity()).isEqualTo("Marrakech");
        assertThat(existingArtisan.getAddress()).isEqualTo("Souk Semmarine");
        assertThat(existingArtisan.getWebsite()).isEqualTo("https://example.com/artisan");
        assertThat(existingArtisan.getSubCategoryId()).isEqualTo("SUBCAT-1");
        assertThat(existingArtisan.isTeacher()).isFalse();
    }

    /**
     * Verifies completeProfile creates new artisan profile when none exists and resolves region from fallback.
     */
    @Test
    @DisplayName("completeProfile: creates new artisan profile with fallback region resolution")
    void completeProfile_forNewArtisan_createsProfileAndReturnsProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-2");

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder()
                .region("REG-FALLBACK")
                .city("Rabat")
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(artisanRepository.findById("artisan-id-2")).thenReturn(Optional.empty());

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        ArgumentCaptor<Artisan> captor = ArgumentCaptor.forClass(Artisan.class);
        verify(artisanRepository).save(captor.capture());
        Artisan created = captor.getValue();
        assertThat(created.getRegionId()).isEqualTo("REG-FALLBACK");
        assertThat(created.getCity()).isEqualTo("Rabat");
    }

    /**
     * Verifies completeProfile updates existing client entity.
     */
    @Test
    @DisplayName("completeProfile: updates existing client profile fields")
    void completeProfile_forExistingClient_updatesAllFieldsAndReturnsProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-1");

        Client existingClient = Client.builder()
                .user(user)
                .build();

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder()
                .clientType("BUSINESS")
                .companyName("Modern Souk")
                .bio("Retail distributor")
                .address("Boulevard Zerktouni")
                .regionId("REG-CASA")
                .city("Casablanca")
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(clientRepository.findById("client-id-1")).thenReturn(Optional.of(existingClient));

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        verify(clientRepository).save(existingClient);
        assertThat(existingClient.getClientType()).isEqualTo("BUSINESS");
        assertThat(existingClient.getCompanyName()).isEqualTo("Modern Souk");
        assertThat(existingClient.getBio()).isEqualTo("Retail distributor");
        assertThat(existingClient.getAddress()).isEqualTo("Boulevard Zerktouni");
        assertThat(existingClient.getRegionId()).isEqualTo("REG-CASA");
        assertThat(existingClient.getCity()).isEqualTo("Casablanca");
    }

    /**
     * Verifies completeProfile creates new client profile when none exists and resolves region from fallback.
     */
    @Test
    @DisplayName("completeProfile: creates new client profile with fallback region resolution")
    void completeProfile_forNewClient_createsProfileAndReturnsProfile() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-2");

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder()
                .region("REG-FALLBACK-CLIENT")
                .city("Tangier")
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(clientRepository.findById("client-id-2")).thenReturn(Optional.empty());

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client created = captor.getValue();
        assertThat(created.getRegionId()).isEqualTo("REG-FALLBACK-CLIENT");
        assertThat(created.getCity()).isEqualTo("Tangier");
    }

    /**
     * Verifies completeProfile returns unmodified profile when user is neither artisan nor client.
     */
    @Test
    @DisplayName("completeProfile: returns unmodified profile when user is neither artisan nor client")
    void completeProfile_whenNeitherArtisanNorClient_returnsProfileWithoutSaving() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        User user = User.builder()
                .email("admin@example.com")
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();
        user.setId("admin-id");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = authService.completeProfile(new CompleteProfileRequestDTO());

        assertThat(response).isNotNull();
        verifyNoInteractions(artisanRepository);
        verifyNoInteractions(clientRepository);
    }

    /**
     * Verifies patchCurrentUser throws UnauthorizedException when unauthenticated.
     */
    @Test
    @DisplayName("patchCurrentUser: throws UnauthorizedException when unauthenticated")
    void patchCurrentUser_whenUnauthenticated_throwsUnauthorizedException() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.patchCurrentUser(mock(JsonNode.class)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Not authenticated.");
    }

    /**
     * Verifies patchCurrentUser throws ResourceNotFoundException when authenticated user not in DB.
     */
    @Test
    @DisplayName("patchCurrentUser: throws ResourceNotFoundException when authenticated user not in DB")
    void patchCurrentUser_whenUserNotFound_throwsResourceNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@example.com", "cred", List.of())
        );

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.patchCurrentUser(mock(JsonNode.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@example.com");
    }

    /**
     * Verifies patchCurrentUser throws ForbiddenException for admin users.
     */
    @Test
    @DisplayName("patchCurrentUser: throws ForbiddenException when user is an administrator")
    void patchCurrentUser_whenUserIsAdmin_throwsForbiddenException() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        User user = User.builder()
                .email("admin@example.com")
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.patchCurrentUser(mock(JsonNode.class)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Administrators do not possess an editable artisan or client profile.");
    }

    /**
     * Verifies patchCurrentUser returns existing profile without modifications when payload is null.
     */
    @Test
    @DisplayName("patchCurrentUser: returns profile without modification when payload is null")
    void patchCurrentUser_whenPayloadIsNull_returnsProfileWithoutModifications() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = authService.patchCurrentUser(null);

        assertThat(response).isNotNull();
        verifyNoInteractions(clientRepository);
    }

    /**
     * Verifies patchCurrentUser returns existing profile when payload is NullNode.
     */
    @Test
    @DisplayName("patchCurrentUser: returns profile without modification when payload is NullNode")
    void patchCurrentUser_whenPayloadIsNullNode_returnsProfileWithoutModifications() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = authService.patchCurrentUser(NullNode.getInstance());

        assertThat(response).isNotNull();
        verifyNoInteractions(clientRepository);
    }

    /**
     * Verifies patchCurrentUser returns existing profile when payload is empty ObjectNode.
     */
    @Test
    @DisplayName("patchCurrentUser: returns profile without modification when payload is empty")
    void patchCurrentUser_whenPayloadIsEmpty_returnsProfileWithoutModifications() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        ObjectNode emptyNode = new JsonMapper().createObjectNode();
        ProfileResponse response = authService.patchCurrentUser(emptyNode);

        assertThat(response).isNotNull();
        verifyNoInteractions(clientRepository);
    }

    /**
     * Verifies patchCurrentUser throws BadRequestException when JSON mapping to ArtisanPatchDTO fails.
     */
    @Test
    @DisplayName("patchCurrentUser: throws BadRequestException when artisan JSON payload cannot be mapped")
    void patchCurrentUser_forArtisan_whenJsonMappingFails_throwsBadRequestException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Test bio");

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class))
                .thenThrow(new IllegalArgumentException("Invalid property"));

        assertThatThrownBy(() -> authService.patchCurrentUser(payload))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("Invalid JSON payload for artisan profile update: Invalid property");
    }

    /**
     * Verifies patchCurrentUser throws ConstraintViolationException when artisan patch validation fails.
     */
    @Test
    @DisplayName("patchCurrentUser: throws ConstraintViolationException when artisan patch validation fails")
    void patchCurrentUser_forArtisan_whenValidationFails_throwsConstraintViolationException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Too long bio");

        ArtisanPatchDTO patchDTO = new ArtisanPatchDTO();
        patchDTO.setBio("Too long bio");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ArtisanPatchDTO> violation = mock(ConstraintViolation.class);

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> authService.patchCurrentUser(payload))
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Verifies patchCurrentUser updates all valid non-null artisan fields.
     */
    @Test
    @DisplayName("patchCurrentUser: updates all valid artisan fields and saves")
    void patchCurrentUser_forArtisan_withValidFields_updatesFieldsAndSaves() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-patch");

        Artisan existingArtisan = Artisan.builder().user(user).build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "New artisan bio");
        payload.put("regionId", "REG-PATCH-1");
        payload.put("city", "Fes");
        payload.put("address", "Derb El Horra");
        payload.put("website", "https://artisan.ma");
        payload.put("subCategoryId", "SUBCAT-POTTERY");

        ArtisanPatchDTO patchDTO = ArtisanPatchDTO.builder()
                .bio("New artisan bio")
                .regionId("REG-PATCH-1")
                .city("Fes")
                .address("Derb El Horra")
                .website("https://artisan.ma")
                .subCategoryId("SUBCAT-POTTERY")
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(artisanRepository.findById("artisan-id-patch")).thenReturn(Optional.of(existingArtisan));

        ProfileResponse response = authService.patchCurrentUser(payload);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        verify(artisanRepository).save(existingArtisan);
        assertThat(existingArtisan.getBio()).isEqualTo("New artisan bio");
        assertThat(existingArtisan.getRegionId()).isEqualTo("REG-PATCH-1");
        assertThat(existingArtisan.getCity()).isEqualTo("Fes");
        assertThat(existingArtisan.getAddress()).isEqualTo("Derb El Horra");
        assertThat(existingArtisan.getWebsite()).isEqualTo("https://artisan.ma");
        assertThat(existingArtisan.getSubCategoryId()).isEqualTo("SUBCAT-POTTERY");
    }

    /**
     * Verifies patchCurrentUser clears artisan fields when explicit nulls are provided in payload.
     */
    @Test
    @DisplayName("patchCurrentUser: clears artisan fields when explicit nulls are submitted")
    void patchCurrentUser_forArtisan_withExplicitNullFields_clearsFieldsAndSaves() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-patch-null");

        Artisan existingArtisan = Artisan.builder()
                .user(user)
                .bio("Old bio")
                .regionId("OLD-REG")
                .city("Old City")
                .address("Old Address")
                .website("https://old.ma")
                .subCategoryId("OLD-SUB")
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.putNull("bio");
        payload.putNull("regionId");
        payload.putNull("city");
        payload.putNull("address");
        payload.putNull("website");
        payload.putNull("subCategoryId");

        ArtisanPatchDTO patchDTO = new ArtisanPatchDTO();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(artisanRepository.findById("artisan-id-patch-null")).thenReturn(Optional.of(existingArtisan));

        authService.patchCurrentUser(payload);

        verify(artisanRepository).save(existingArtisan);
        assertThat(existingArtisan.getBio()).isNull();
        assertThat(existingArtisan.getRegionId()).isNull();
        assertThat(existingArtisan.getCity()).isNull();
        assertThat(existingArtisan.getAddress()).isNull();
        assertThat(existingArtisan.getWebsite()).isNull();
        assertThat(existingArtisan.getSubCategoryId()).isNull();
    }

    /**
     * Verifies patchCurrentUser updates artisan regionId when region key is provided instead of regionId.
     */
    @Test
    @DisplayName("patchCurrentUser: updates artisan regionId using region key fallback")
    void patchCurrentUser_forArtisan_withRegionFallback_updatesRegionId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-region-fallback");

        Artisan existingArtisan = Artisan.builder().user(user).build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("region", "REG-FALLBACK-ARTISAN");

        ArtisanPatchDTO patchDTO = ArtisanPatchDTO.builder()
                .region("REG-FALLBACK-ARTISAN")
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(artisanRepository.findById("artisan-id-region-fallback")).thenReturn(Optional.of(existingArtisan));

        authService.patchCurrentUser(payload);

        assertThat(existingArtisan.getRegionId()).isEqualTo("REG-FALLBACK-ARTISAN");
    }

    /**
     * Verifies patchCurrentUser clears artisan regionId when explicit null is passed under region key.
     */
    @Test
    @DisplayName("patchCurrentUser: clears artisan regionId when explicit null is passed in region fallback key")
    void patchCurrentUser_forArtisan_withExplicitNullRegion_clearsRegionId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-id-region-null");

        Artisan existingArtisan = Artisan.builder().user(user).regionId("OLD-REG").build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.putNull("region");

        ArtisanPatchDTO patchDTO = new ArtisanPatchDTO();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(artisanRepository.findById("artisan-id-region-null")).thenReturn(Optional.of(existingArtisan));

        authService.patchCurrentUser(payload);

        assertThat(existingArtisan.getRegionId()).isNull();
    }

    /**
     * Verifies patchCurrentUser throws BadRequestException when JSON mapping to ClientPatchDTO fails.
     */
    @Test
    @DisplayName("patchCurrentUser: throws BadRequestException when client JSON payload cannot be mapped")
    void patchCurrentUser_forClient_whenJsonMappingFails_throwsBadRequestException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Client bio");

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class))
                .thenThrow(new IllegalArgumentException("Invalid client property"));

        assertThatThrownBy(() -> authService.patchCurrentUser(payload))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("Invalid JSON payload for client profile update: Invalid client property");
    }

    /**
     * Verifies patchCurrentUser throws ConstraintViolationException when client patch validation fails.
     */
    @Test
    @DisplayName("patchCurrentUser: throws ConstraintViolationException when client patch validation fails")
    void patchCurrentUser_forClient_whenValidationFails_throwsConstraintViolationException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("companyName", "Bad Company Name");

        ClientPatchDTO patchDTO = new ClientPatchDTO();

        @SuppressWarnings("unchecked")
        ConstraintViolation<ClientPatchDTO> violation = mock(ConstraintViolation.class);

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> authService.patchCurrentUser(payload))
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Verifies patchCurrentUser updates all valid non-null client fields.
     */
    @Test
    @DisplayName("patchCurrentUser: updates all valid client fields and saves")
    void patchCurrentUser_forClient_withValidFields_updatesFieldsAndSaves() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-patch");

        Client existingClient = Client.builder().user(user).build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Corporate buyer bio");
        payload.put("address", "123 Commercial Ave");
        payload.put("regionId", "REG-CLIENT-1");
        payload.put("city", "Rabat");
        payload.put("companyName", "Artisanal Exports");
        payload.put("clientType", "ENTERPRISE");

        ClientPatchDTO patchDTO = ClientPatchDTO.builder()
                .bio("Corporate buyer bio")
                .address("123 Commercial Ave")
                .regionId("REG-CLIENT-1")
                .city("Rabat")
                .companyName("Artisanal Exports")
                .clientType("ENTERPRISE")
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(clientRepository.findById("client-id-patch")).thenReturn(Optional.of(existingClient));

        ProfileResponse response = authService.patchCurrentUser(payload);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        verify(clientRepository).save(existingClient);
        assertThat(existingClient.getBio()).isEqualTo("Corporate buyer bio");
        assertThat(existingClient.getAddress()).isEqualTo("123 Commercial Ave");
        assertThat(existingClient.getRegionId()).isEqualTo("REG-CLIENT-1");
        assertThat(existingClient.getCity()).isEqualTo("Rabat");
        assertThat(existingClient.getCompanyName()).isEqualTo("Artisanal Exports");
        assertThat(existingClient.getClientType()).isEqualTo("ENTERPRISE");
    }

    /**
     * Verifies patchCurrentUser clears client fields when explicit nulls are provided in payload.
     */
    @Test
    @DisplayName("patchCurrentUser: clears client fields when explicit nulls are submitted")
    void patchCurrentUser_forClient_withExplicitNullFields_clearsFieldsAndSaves() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-patch-null");

        Client existingClient = Client.builder()
                .user(user)
                .bio("Old bio")
                .address("Old address")
                .regionId("OLD-REG")
                .city("Old city")
                .companyName("Old Corp")
                .clientType("BUSINESS")
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.putNull("bio");
        payload.putNull("address");
        payload.putNull("regionId");
        payload.putNull("city");
        payload.putNull("companyName");
        payload.putNull("clientType");

        ClientPatchDTO patchDTO = new ClientPatchDTO();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(clientRepository.findById("client-id-patch-null")).thenReturn(Optional.of(existingClient));

        authService.patchCurrentUser(payload);

        verify(clientRepository).save(existingClient);
        assertThat(existingClient.getBio()).isNull();
        assertThat(existingClient.getAddress()).isNull();
        assertThat(existingClient.getRegionId()).isNull();
        assertThat(existingClient.getCity()).isNull();
        assertThat(existingClient.getCompanyName()).isNull();
        assertThat(existingClient.getClientType()).isNull();
    }

    /**
     * Verifies patchCurrentUser updates client regionId when region key is provided instead of regionId.
     */
    @Test
    @DisplayName("patchCurrentUser: updates client regionId using region key fallback")
    void patchCurrentUser_forClient_withRegionFallback_updatesRegionId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-region-fallback");

        Client existingClient = Client.builder().user(user).build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("region", "REG-FALLBACK-CLIENT");

        ClientPatchDTO patchDTO = ClientPatchDTO.builder()
                .region("REG-FALLBACK-CLIENT")
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(clientRepository.findById("client-id-region-fallback")).thenReturn(Optional.of(existingClient));

        authService.patchCurrentUser(payload);

        assertThat(existingClient.getRegionId()).isEqualTo("REG-FALLBACK-CLIENT");
    }

    /**
     * Verifies patchCurrentUser clears client regionId when explicit null is passed under region key.
     */
    @Test
    @DisplayName("patchCurrentUser: clears client regionId when explicit null is passed in region fallback key")
    void patchCurrentUser_forClient_withExplicitNullRegion_clearsRegionId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-id-region-null");

        Client existingClient = Client.builder().user(user).regionId("OLD-REG").build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.putNull("region");

        ClientPatchDTO patchDTO = new ClientPatchDTO();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(clientRepository.findById("client-id-region-null")).thenReturn(Optional.of(existingClient));

        authService.patchCurrentUser(payload);

        assertThat(existingClient.getRegionId()).isNull();
    }

    /**
     * Verifies processOAuth2Success throws BadRequestException when provider returns null or blank email.
     */
    @Test
    @DisplayName("processOAuth2Success: throws BadRequestException when email missing from OAuth provider")
    void processOAuth2Success_whenEmailMissingFromOAuthUser_throwsBadRequestException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-123");
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("OAuth provider did not return an email address.");
    }

    /**
     * Verifies processOAuth2Success logs in existing user matching provider and sub ID.
     */
    @Test
    @DisplayName("processOAuth2Success: logs in existing user matching OAuth identity")
    void processOAuth2Success_whenOAuthIdentityExists_logsInExistingUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-123");
        when(oAuth2User.getAttribute("email")).thenReturn("existing@example.com");

        User existingUser = User.builder()
                .email("existing@example.com")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        OAuthIdentity identity = OAuthIdentity.builder()
                .user(existingUser)
                .provider("GOOGLE")
                .providerUserId("google-sub-123")
                .build();

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-123"))
                .thenReturn(Optional.of(identity));
        when(jwtUtils.generateAccessToken("existing@example.com")).thenReturn("access-token-oauth");
        when(refreshTokenService.createRefreshTokenForUser(existingUser))
                .thenReturn(RefreshToken.builder().token("refresh-token-oauth").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, null, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-oauth");
        assertThat(existingUser.getLastLoginAt()).isEqualTo(fixedNow);
        verify(userRepository).save(existingUser);
    }

    /**
     * Verifies processOAuth2Success falls back to getName when sub attribute is blank.
     */
    @Test
    @DisplayName("processOAuth2Success: falls back to getName when sub attribute is blank")
    void processOAuth2Success_whenSubAttributeBlank_fallsBackToOAuthUserName() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("   ");
        when(oAuth2User.getName()).thenReturn("google-name-456");
        when(oAuth2User.getAttribute("email")).thenReturn("existing@example.com");

        User existingUser = User.builder()
                .email("existing@example.com")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        OAuthIdentity identity = OAuthIdentity.builder()
                .user(existingUser)
                .provider("GOOGLE")
                .providerUserId("google-name-456")
                .build();

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-name-456"))
                .thenReturn(Optional.of(identity));
        when(jwtUtils.generateAccessToken("existing@example.com")).thenReturn("access-token-oauth");
        when(refreshTokenService.createRefreshTokenForUser(existingUser))
                .thenReturn(RefreshToken.builder().token("refresh-token-oauth").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, null, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-oauth");
    }

    /**
     * Verifies processOAuth2Success auto-links identity to existing user when email matches.
     */
    @Test
    @DisplayName("processOAuth2Success: auto-links OAuth identity to existing user by email")
    void processOAuth2Success_whenUserExistsByEmail_linksOAuthIdentityAndLogsIn() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-new");
        when(oAuth2User.getAttribute("email")).thenReturn("match@example.com");

        User existingUser = User.builder()
                .email("match@example.com")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-new"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("match@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtUtils.generateAccessToken("match@example.com")).thenReturn("access-token-match");
        when(refreshTokenService.createRefreshTokenForUser(existingUser))
                .thenReturn(RefreshToken.builder().token("refresh-token-match").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, null, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-match");
        ArgumentCaptor<OAuthIdentity> captor = ArgumentCaptor.forClass(OAuthIdentity.class);
        verify(oauthIdentityRepository).save(captor.capture());
        OAuthIdentity linked = captor.getValue();
        assertThat(linked.getProvider()).isEqualTo("GOOGLE");
        assertThat(linked.getProviderUserId()).isEqualTo("google-sub-new");
        assertThat(linked.getUser()).isEqualTo(existingUser);
    }

    /**
     * Verifies processOAuth2Success throws BadRequestException for new user when intent role is null.
     */
    @Test
    @DisplayName("processOAuth2Success: throws BadRequestException when new user has null intent role")
    void processOAuth2Success_whenNewUserAndIntentRoleNull_throwsBadRequestException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-999");
        when(oAuth2User.getAttribute("email")).thenReturn("newuser@example.com");

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-999"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("OAuth registration intent not found or expired.");
    }

    /**
     * Verifies processOAuth2Success throws BadRequestException for new user when intent role is invalid.
     */
    @Test
    @DisplayName("processOAuth2Success: throws BadRequestException when new user has invalid intent role")
    void processOAuth2Success_whenNewUserAndIntentRoleInvalid_throwsBadRequestException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-999");
        when(oAuth2User.getAttribute("email")).thenReturn("newuser@example.com");

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-999"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, "SUPERUSER", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid OAuth registration role intent: SUPERUSER");
    }

    /**
     * Verifies processOAuth2Success creates pending artisan for new user with artisan intent.
     */
    @Test
    @DisplayName("processOAuth2Success: creates pending artisan user for artisan intent")
    void processOAuth2Success_whenNewUserWithArtisanIntent_createsPendingArtisanAndLogsIn() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-artisan");
        when(oAuth2User.getAttribute("email")).thenReturn("newartisan@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("Ahmed");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Tazi");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://photos.google.com/ahmed.jpg");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-artisan"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newartisan@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_ARTISAN")).thenReturn(Optional.of(artisanRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtils.generateAccessToken("newartisan@example.com")).thenReturn("access-token-new");
        when(refreshTokenService.createRefreshTokenForUser(any(User.class)))
                .thenReturn(RefreshToken.builder().token("refresh-token-new").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, "ARTISAN", request);

        assertThat(response.getAccessToken()).isEqualTo("access-token-new");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("newartisan@example.com");
        assertThat(savedUser.getPassword()).isNull();
        assertThat(savedUser.getFirstName()).isEqualTo("Ahmed");
        assertThat(savedUser.getLastName()).isEqualTo("Tazi");
        assertThat(savedUser.getAvatarUrl()).isEqualTo("https://photos.google.com/ahmed.jpg");
        assertThat(savedUser.getStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(savedUser.isEmailVerified()).isTrue();
        assertThat(savedUser.getEmailVerifiedAt()).isEqualTo(fixedNow);
        assertThat(savedUser.getLastLoginIp()).isEqualTo("10.0.0.1");

        ArgumentCaptor<OAuthIdentity> identityCaptor = ArgumentCaptor.forClass(OAuthIdentity.class);
        verify(oauthIdentityRepository).save(identityCaptor.capture());
        OAuthIdentity savedIdentity = identityCaptor.getValue();
        assertThat(savedIdentity.getProvider()).isEqualTo("GOOGLE");
        assertThat(savedIdentity.getProviderUserId()).isEqualTo("google-sub-artisan");
        assertThat(savedIdentity.getEmail()).isEqualTo("newartisan@example.com");
        assertThat(savedIdentity.getUser()).isEqualTo(savedUser);
    }

    /**
     * Verifies processOAuth2Success creates active client for new user with client intent.
     */
    @Test
    @DisplayName("processOAuth2Success: creates active client user for client intent")
    void processOAuth2Success_whenNewUserWithClientIntent_createsActiveClientAndLogsIn() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-client");
        when(oAuth2User.getAttribute("email")).thenReturn("newclient@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("Laila");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Fassi");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-client"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newclient@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtils.generateAccessToken("newclient@example.com")).thenReturn("access-token-client");
        when(refreshTokenService.createRefreshTokenForUser(any(User.class)))
                .thenReturn(RefreshToken.builder().token("refresh-token-client").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, "CLIENT", null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-client");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("newclient@example.com");
        assertThat(savedUser.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(savedUser.isEmailVerified()).isTrue();
        assertThat(savedUser.getEmailVerifiedAt()).isEqualTo(fixedNow);

        ArgumentCaptor<OAuthIdentity> identityCaptor = ArgumentCaptor.forClass(OAuthIdentity.class);
        verify(oauthIdentityRepository).save(identityCaptor.capture());
        OAuthIdentity savedIdentity = identityCaptor.getValue();
        assertThat(savedIdentity.getProvider()).isEqualTo("GOOGLE");
        assertThat(savedIdentity.getProviderUserId()).isEqualTo("google-sub-client");
        assertThat(savedIdentity.getEmail()).isEqualTo("newclient@example.com");
        assertThat(savedIdentity.getUser()).isEqualTo(savedUser);
    }

    /**
     * Verifies processOAuth2Success throws ResourceNotFoundException when role is not in repository.
     */
    @Test
    @DisplayName("processOAuth2Success: throws ResourceNotFoundException when assigned role is missing")
    void processOAuth2Success_whenNewUserAndRoleNotFoundInRepository_throwsResourceNotFoundException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-role-missing");
        when(oAuth2User.getAttribute("email")).thenReturn("norole@example.com");

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-role-missing"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("norole@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_ARTISAN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, "ARTISAN", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found: ROLE_ARTISAN");
    }

    /**
     * Verifies mapToProfileResponse returns ArtisanResponseDTO with default values when Artisan entity is null.
     */
    @Test
    @DisplayName("mapToProfileResponse: returns ArtisanResponseDTO with defaults when Artisan entity is null")
    void mapToProfileResponse_forArtisanWithNullArtisanEntity_returnsDefaults() {
        User user = User.builder()
                .email("artisan@example.com")
                .firstName("Ali")
                .lastName("K")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .artisan(null)
                .build();

        ProfileResponse response = authService.mapToProfileResponse(user);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        ArtisanResponseDTO dto = (ArtisanResponseDTO) response;
        assertThat(dto.getBio()).isNull();
        assertThat(dto.getRating()).isEqualTo(0.0);
        assertThat(dto.getReviewsCount()).isZero();
        assertThat(dto.isTeacher()).isFalse();
        assertThat(dto.isVerified()).isFalse();
        assertThat(dto.isPremium()).isFalse();
    }

    /**
     * Verifies mapToProfileResponse returns ArtisanResponseDTO with full values when Artisan entity is populated.
     */
    @Test
    @DisplayName("mapToProfileResponse: returns ArtisanResponseDTO with populated entity fields")
    void mapToProfileResponse_forArtisanWithPopulatedArtisanEntity_returnsMappedFields() {
        Artisan artisan = Artisan.builder()
                .bio("Master potter")
                .city("Safi")
                .rating(4.9)
                .reviewsCount(50)
                .isTeacher(true)
                .isVerified(true)
                .isPremium(true)
                .build();

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .artisan(artisan)
                .build();

        ProfileResponse response = authService.mapToProfileResponse(user);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        ArtisanResponseDTO dto = (ArtisanResponseDTO) response;
        assertThat(dto.getBio()).isEqualTo("Master potter");
        assertThat(dto.getRating()).isEqualTo(4.9);
        assertThat(dto.getReviewsCount()).isEqualTo(50);
        assertThat(dto.isTeacher()).isTrue();
        assertThat(dto.isVerified()).isTrue();
        assertThat(dto.isPremium()).isTrue();
    }

    /**
     * Verifies mapToProfileResponse returns ClientProfileResponseDTO with default clientType INDIVIDUAL when Client is null.
     */
    @Test
    @DisplayName("mapToProfileResponse: returns ClientProfileResponseDTO with INDIVIDUAL default when Client is null")
    void mapToProfileResponse_forClientWithNullClientEntity_returnsDefaults() {
        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .client(null)
                .build();

        ProfileResponse response = authService.mapToProfileResponse(user);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ClientProfileResponseDTO dto = (ClientProfileResponseDTO) response;
        assertThat(dto.getClientType()).isEqualTo("INDIVIDUAL");
        assertThat(dto.getCompanyName()).isNull();
    }

    /**
     * Verifies mapToProfileResponse returns ClientProfileResponseDTO with populated Client fields.
     */
    @Test
    @DisplayName("mapToProfileResponse: returns ClientProfileResponseDTO with populated fields")
    void mapToProfileResponse_forClientWithPopulatedClientEntity_returnsMappedFields() {
        Client client = Client.builder()
                .companyName("Heritage Imports")
                .clientType("BUSINESS")
                .city("Tangier")
                .build();

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .client(client)
                .build();

        ProfileResponse response = authService.mapToProfileResponse(user);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        ClientProfileResponseDTO dto = (ClientProfileResponseDTO) response;
        assertThat(dto.getCompanyName()).isEqualTo("Heritage Imports");
        assertThat(dto.getClientType()).isEqualTo("BUSINESS");
        assertThat(dto.getCity()).isEqualTo("Tangier");
    }

    /**
     * Verifies mapToLoginSummary delegates to mapToProfileResponse.
     */
    @Test
    @DisplayName("mapToLoginSummary: delegates directly to mapToProfileResponse")
    void mapToLoginSummary_delegatesToMapToProfileResponse() {
        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        ProfileResponse summary = authService.mapToLoginSummary(user);
        ProfileResponse profile = authService.mapToProfileResponse(user);

        assertThat(summary.getEmail()).isEqualTo(profile.getEmail());
    }

    /**
     * Verifies mapToSummaryDTO maps artisan role, teaching flag, verification, and premium flags.
     */
    @Test
    @DisplayName("mapToSummaryDTO: maps artisan flags and roles correctly")
    void mapToSummaryDTO_withArtisanAndFlags_mapsExpectedSummary() {
        Artisan artisan = Artisan.builder()
                .isTeacher(true)
                .isPremium(true)
                .isVerified(true)
                .build();

        User user = User.builder()
                .email("artisan@example.com")
                .firstName("Rachid")
                .lastName("M")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .artisan(artisan)
                .build();

        UserSummaryDTO summary = authService.mapToSummaryDTO(user);

        assertThat(summary.getEmail()).isEqualTo("artisan@example.com");
        assertThat(summary.getRole()).isEqualTo("ROLE_ARTISAN");
        assertThat(summary.isTeacher()).isTrue();
        assertThat(summary.isPremium()).isTrue();
        assertThat(summary.isValidated()).isTrue();
    }

    /**
     * Verifies mapToSummaryDTO maps client flags correctly.
     */
    @Test
    @DisplayName("mapToSummaryDTO: maps client flags correctly")
    void mapToSummaryDTO_withClientAndFlags_mapsExpectedSummary() {
        Client client = Client.builder()
                .isPremium(true)
                .isVerified(true)
                .build();

        User user = User.builder()
                .email("client@example.com")
                .firstName("Fatima")
                .lastName("Z")
                .roles(new HashSet<>(Set.of(clientRole)))
                .client(client)
                .build();

        UserSummaryDTO summary = authService.mapToSummaryDTO(user);

        assertThat(summary.getRole()).isEqualTo("ROLE_CLIENT");
        assertThat(summary.isTeacher()).isFalse();
        assertThat(summary.isPremium()).isTrue();
        assertThat(summary.isValidated()).isTrue();
    }

    /**
     * Verifies mapToSummaryDTO defaults primary role to ROLE_CLIENT when user has no roles.
     */
    @Test
    @DisplayName("mapToSummaryDTO: defaults primary role to ROLE_CLIENT when roles set is empty")
    void mapToSummaryDTO_withNoRoles_defaultsPrimaryRoleToClient() {
        User user = User.builder()
                .email("noroles@example.com")
                .roles(new HashSet<>())
                .build();

        UserSummaryDTO summary = authService.mapToSummaryDTO(user);

        assertThat(summary.getRole()).isEqualTo("ROLE_CLIENT");
    }

    /**
     * Verifies verifyEmail throws ResourceNotFoundException when user is not found.
     */
    @Test
    @DisplayName("verifyEmail: throws ResourceNotFoundException when user not found")
    void verifyEmail_whenUserNotFound_throwsResourceNotFoundException() {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("missing@example.com")
                .code("123456")
                .build();

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: missing@example.com");
    }

    /**
     * Verifies verifyEmail propagates BadRequestException when token service rejects code.
     */
    @Test
    @DisplayName("verifyEmail: propagates BadRequestException when code is invalid or expired")
    void verifyEmail_whenTokenServiceRejectsCode_propagatesException() {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("user@example.com")
                .code("000000")
                .build();

        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doThrow(new BadRequestException("Invalid code"))
                .when(verificationTokenService).validateAndConsume(user, VerificationTokenType.EMAIL_VERIFICATION, "000000");

        assertThatThrownBy(() -> authService.verifyEmail(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid code");
    }

    /**
     * Verifies verifyEmail marks email as verified, records timestamp, saves user and logs audit entry.
     */
    @Test
    @DisplayName("verifyEmail: marks email verified and records audit log")
    void verifyEmail_withValidCode_marksEmailVerifiedAndLogsAudit() {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("user@example.com")
                .code("654321")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        authService.verifyEmail(dto);

        verify(verificationTokenService).validateAndConsume(user, VerificationTokenType.EMAIL_VERIFICATION, "654321");
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isEqualTo(fixedNow);
        verify(userRepository).save(user);
        verify(auditLogService).logAction(AuditLogAction.EMAIL_VERIFIED,
                "Email verified for user: user@example.com", "user@example.com");
    }

    /**
     * Verifies resendVerification returns silently when email is not registered (anti-enumeration).
     */
    @Test
    @DisplayName("resendVerification: returns silently when user does not exist")
    void resendVerification_whenUserNotFound_returnsSilentlyWithoutError() {
        ResendVerificationRequestDTO dto = ResendVerificationRequestDTO.builder()
                .email("ghost@example.com")
                .build();

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        authService.resendVerification(dto);

        verifyNoInteractions(verificationTokenService);
        verifyNoInteractions(emailUtil);
    }

    /**
     * Verifies resendVerification returns silently without issuing code when user is already verified.
     */
    @Test
    @DisplayName("resendVerification: returns silently when user is already verified")
    void resendVerification_whenUserAlreadyVerified_doesNotIssueTokenOrSendEmail() {
        ResendVerificationRequestDTO dto = ResendVerificationRequestDTO.builder()
                .email("verified@example.com")
                .build();

        User user = User.builder()
                .email("verified@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(user));

        authService.resendVerification(dto);

        verifyNoInteractions(verificationTokenService);
        verifyNoInteractions(emailUtil);
    }

    /**
     * Verifies resendVerification issues token and sends code when user is unverified.
     */
    @Test
    @DisplayName("resendVerification: issues token and dispatches email when user is unverified")
    void resendVerification_whenUserUnverified_issuesTokenAndSendsVerificationEmail() {
        ResendVerificationRequestDTO dto = ResendVerificationRequestDTO.builder()
                .email("unverified@example.com")
                .build();

        User user = User.builder()
                .email("unverified@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.issueToken(user, VerificationTokenType.EMAIL_VERIFICATION))
                .thenReturn("888999");

        authService.resendVerification(dto);

        verify(emailUtil).sendVerificationCode("unverified@example.com", "888999");
    }

    /**
     * Verifies resendVerification catches email dispatch failure without throwing exception.
     */
    @Test
    @DisplayName("resendVerification: catches email sending exception and does not propagate")
    void resendVerification_whenEmailSendFails_catchesExceptionAndDoesNotThrow() {
        ResendVerificationRequestDTO dto = ResendVerificationRequestDTO.builder()
                .email("unverified@example.com")
                .build();

        User user = User.builder()
                .email("unverified@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.issueToken(user, VerificationTokenType.EMAIL_VERIFICATION))
                .thenReturn("888999");
        doThrow(new RuntimeException("SMTP connection timeout"))
                .when(emailUtil).sendVerificationCode(anyString(), anyString());

        authService.resendVerification(dto);
    }

    /**
     * Verifies forgotPassword returns silently when email does not exist (anti-enumeration).
     */
    @Test
    @DisplayName("forgotPassword: returns silently when user does not exist")
    void forgotPassword_whenUserNotFound_returnsSilentlyWithoutError() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("unknown@example.com")
                .build();

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(dto);

        verifyNoInteractions(verificationTokenService);
        verifyNoInteractions(emailUtil);
    }

    /**
     * Verifies forgotPassword issues reset token and sends code when user has a password.
     */
    @Test
    @DisplayName("forgotPassword: issues password reset token and sends email when user has password")
    void forgotPassword_whenUserHasPassword_issuesTokenAndSendsResetCodeEmail() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("user@example.com")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.issueToken(user, VerificationTokenType.PASSWORD_RESET))
                .thenReturn("777111");

        authService.forgotPassword(dto);

        verify(emailUtil).sendPasswordResetCode("user@example.com", "777111");
    }

    /**
     * Verifies forgotPassword sends informational notice when user has no password (OAuth-only).
     */
    @Test
    @DisplayName("forgotPassword: sends OAuth-only notice when user has no password")
    void forgotPassword_whenUserHasNoPassword_sendsOAuthNoticeEmail() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("oauthonly@example.com")
                .build();

        User user = User.builder()
                .email("oauthonly@example.com")
                .password(null)
                .build();

        when(userRepository.findByEmail("oauthonly@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(dto);

        verify(emailUtil).sendOAuthOnlyPasswordResetNotice("oauthonly@example.com");
        verifyNoInteractions(verificationTokenService);
    }

    /**
     * Verifies forgotPassword catches reset email failure without throwing.
     */
    @Test
    @DisplayName("forgotPassword: catches reset email dispatch failure without throwing")
    void forgotPassword_whenPasswordResetEmailFails_catchesExceptionAndDoesNotThrow() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("user@example.com")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.issueToken(user, VerificationTokenType.PASSWORD_RESET))
                .thenReturn("777111");
        doThrow(new RuntimeException("Mail failure")).when(emailUtil).sendPasswordResetCode(anyString(), anyString());

        authService.forgotPassword(dto);
    }

    /**
     * Verifies forgotPassword catches OAuth notice email failure without throwing.
     */
    @Test
    @DisplayName("forgotPassword: catches OAuth notice dispatch failure without throwing")
    void forgotPassword_whenOAuthNoticeEmailFails_catchesExceptionAndDoesNotThrow() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("oauthonly@example.com")
                .build();

        User user = User.builder()
                .email("oauthonly@example.com")
                .password("")
                .build();

        when(userRepository.findByEmail("oauthonly@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Mail failure")).when(emailUtil).sendOAuthOnlyPasswordResetNotice(anyString());

        authService.forgotPassword(dto);
    }

    /**
     * Verifies resetPassword throws BadRequestException when email is not found.
     */
    @Test
    @DisplayName("resetPassword: throws BadRequestException when user not found")
    void resetPassword_whenUserNotFound_throwsBadRequestException() {
        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .email("missing@example.com")
                .code("123456")
                .newPassword("brandNewPassword123")
                .build();

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid or expired code.");
    }

    /**
     * Verifies resetPassword propagates BadRequestException when verification code is invalid.
     */
    @Test
    @DisplayName("resetPassword: propagates BadRequestException when code validation fails")
    void resetPassword_whenTokenServiceRejectsCode_propagatesException() {
        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .email("user@example.com")
                .code("000000")
                .newPassword("brandNewPassword123")
                .build();

        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doThrow(new BadRequestException("Code expired"))
                .when(verificationTokenService).validateAndConsume(user, VerificationTokenType.PASSWORD_RESET, "000000");

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Code expired");
    }

    /**
     * Verifies resetPassword updates password, deletes refresh tokens, and records audit log.
     */
    @Test
    @DisplayName("resetPassword: encodes password, deletes refresh tokens, and records audit log")
    void resetPassword_withValidCode_encodesPasswordDeletesTokensAndLogsAudit() {
        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .email("user@example.com")
                .code("555444")
                .newPassword("brandNewPassword123")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("oldHashedPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("brandNewPassword123")).thenReturn("newlyHashedPassword");

        authService.resetPassword(dto);

        verify(verificationTokenService).validateAndConsume(user, VerificationTokenType.PASSWORD_RESET, "555444");
        assertThat(user.getPassword()).isEqualTo("newlyHashedPassword");
        verify(userRepository).save(user);
        verify(refreshTokenService).deleteByUser(user);
        verify(auditLogService).logAction(AuditLogAction.PASSWORD_RESET_COMPLETED,
                "Password reset completed for user: user@example.com", "user@example.com");
    }

    /**
     * Verifies changePassword throws UnauthorizedException when user is unauthenticated.
     */
    @Test
    @DisplayName("changePassword: throws UnauthorizedException when unauthenticated")
    void changePassword_whenUnauthenticated_throwsUnauthorizedException() {
        SecurityContextHolder.clearContext();

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Not authenticated.");
    }

    /**
     * Verifies changePassword throws ResourceNotFoundException when authenticated user not in DB.
     */
    @Test
    @DisplayName("changePassword: throws ResourceNotFoundException when authenticated user not in DB")
    void changePassword_whenUserNotFound_throwsResourceNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@example.com");
    }

    /**
     * Verifies changePassword rejects OAuth-only users who have no existing password.
     */
    @Test
    @DisplayName("changePassword: throws BadRequestException when user has null password")
    void changePassword_whenUserHasNoPassword_throwsBadRequestException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("oauth@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        User user = User.builder()
                .email("oauth@example.com")
                .password(null)
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("This account was created via social login");
    }

    /**
     * Verifies changePassword rejects OAuth-only users who have blank existing password.
     */
    @Test
    @DisplayName("changePassword: throws BadRequestException when user has blank password")
    void changePassword_whenUserHasBlankPassword_throwsBadRequestException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("oauth@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        User user = User.builder()
                .email("oauth@example.com")
                .password("   ")
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("This account was created via social login");
    }

    /**
     * Verifies changePassword throws BadRequestException when current password is incorrect.
     */
    @Test
    @DisplayName("changePassword: throws BadRequestException when old password does not match")
    void changePassword_whenOldPasswordIncorrect_throwsBadRequestException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("wrongCurrentPassword")
                .newPassword("newValidPassword123")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedCurrentPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongCurrentPassword", "hashedCurrentPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Current password is incorrect.");
    }

    /**
     * Verifies changePassword updates password, deletes tokens, sends notification, and records audit entry.
     */
    @Test
    @DisplayName("changePassword: updates password, revokes tokens, sends notice, and logs audit")
    void changePassword_withValidRequest_updatesPasswordDeletesTokensSendsNoticeAndLogsAudit() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("correctOldPassword123")
                .newPassword("brandNewPassword456")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedOldPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctOldPassword123", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("brandNewPassword456")).thenReturn("hashedNewPassword");

        authService.changePassword(request);

        assertThat(user.getPassword()).isEqualTo("hashedNewPassword");
        verify(userRepository).save(user);
        verify(refreshTokenService).deleteByUser(user);
        verify(emailUtil).sendPasswordChangedNotice("user@example.com");
        verify(auditLogService).logAction(AuditLogAction.PASSWORD_CHANGED,
                "Password changed for user: user@example.com", "user@example.com");
    }

    /**
     * Verifies changePassword catches email notification failure and completes successfully.
     */
    @Test
    @DisplayName("changePassword: catches email dispatch failure and completes audit")
    void changePassword_whenEmailNoticeFails_catchesExceptionAndCompletesSuccessfully() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "cred", List.of())
        );

        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("correctOldPassword123")
                .newPassword("brandNewPassword456")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedOldPassword")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctOldPassword123", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("brandNewPassword456")).thenReturn("hashedNewPassword");
        doThrow(new RuntimeException("Mail server down"))
                .when(emailUtil).sendPasswordChangedNotice(anyString());

        authService.changePassword(request);

        verify(auditLogService).logAction(AuditLogAction.PASSWORD_CHANGED,
                "Password changed for user: user@example.com", "user@example.com");
    }

    /**
     * Verifies registerUser rejects registration when role in DTO is null.
     */
    @Test
    @DisplayName("registerUser: throws BadRequestException when role is null")
    void registerUser_whenRoleIsNull_throwsBadRequestException() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("norole@example.com")
                .password("password123")
                .role(null)
                .build();

        when(userRepository.existsByEmail("norole@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid registration role. Allowed roles are ARTISAN or CLIENT.");
    }

    /**
     * Verifies registerUser splits full name when firstName is blank.
     */
    @Test
    @DisplayName("registerUser: splits full name when firstName is blank")
    void registerUser_withBlankFirstNameAndPopulatedName_splitsName() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .firstName("   ")
                .name("Karim Bensalem")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });

        ProfileResponse response = authService.registerUser(dto);

        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getFirstName()).isEqualTo("Karim");
        assertThat(clientProfile.getLastName()).isEqualTo("Bensalem");
    }

    /**
     * Verifies registerUser does not split name when both firstName and name are null.
     */
    @Test
    @DisplayName("registerUser: preserves null names when firstName and name are null")
    void registerUser_withNullFirstNameAndNullName_keepsEmptyOrNullNames() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .firstName(null)
                .name(null)
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });

        ProfileResponse response = authService.registerUser(dto);

        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getFirstName()).isNull();
        assertThat(clientProfile.getLastName()).isNull();
    }

    /**
     * Verifies registerUser does not split name when firstName is null and name is blank.
     */
    @Test
    @DisplayName("registerUser: does not split name when firstName is null and name is blank")
    void registerUser_withNullFirstNameAndBlankName_keepsEmptyOrNullNames() {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .email("client@example.com")
                .password("rawPassword123")
                .firstName(null)
                .name("   ")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("client@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("client-user-id");
            return user;
        });

        ProfileResponse response = authService.registerUser(dto);

        ClientProfileResponseDTO clientProfile = (ClientProfileResponseDTO) response;
        assertThat(clientProfile.getFirstName()).isNull();
        assertThat(clientProfile.getLastName()).isNull();
    }

    /**
     * Verifies login throws BadRequestException when DTO returns non-null blank identifier.
     */
    @Test
    @DisplayName("login: throws BadRequestException when identifier returned is blank string")
    void login_whenIdentifierIsBlankFromDTO_throwsBadRequestException() {
        LoginDTO mockDto = mock(LoginDTO.class);
        when(mockDto.getLoginIdentifier()).thenReturn("   ");

        assertThatThrownBy(() -> authService.login(mockDto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is required for login.");
    }

    /**
     * Verifies login increments attempts counter sequentially across attempts 1 through 4 without locking.
     */
    @Test
    @DisplayName("login: increments failed attempts counter sequentially without locking across attempts 1-4")
    void login_whenPasswordIncorrectMultipleTimes_incrementsAttemptsSequentially() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .failedLoginAttempts(2)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto, null))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
    }

    /**
     * Verifies login falls back to remote address when X-Forwarded-For header is present but blank.
     */
    @Test
    @DisplayName("login: falls back to remoteAddr when X-Forwarded-For header is blank")
    void login_withBlankXForwardedForHeader_fallsBackToRemoteAddr() {
        LoginDTO dto = LoginDTO.builder()
                .email("user@example.com")
                .password("correctPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("10.20.30.40");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(refreshTokenService.createRefreshTokenForUser(user))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        authService.login(dto, request);

        assertThat(user.getLastLoginIp()).isEqualTo("10.20.30.40");
    }

    /**
     * Verifies completeProfile preserves existing artisan regionId and city when DTO fields are null.
     */
    @Test
    @DisplayName("completeProfile: preserves existing artisan regionId and city when optional DTO fields are null")
    void completeProfile_forArtisan_withNullOptionalFields_preservesExistingValues() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-preserve-id");

        Artisan existingArtisan = Artisan.builder()
                .user(user)
                .regionId("PRESERVED-REG")
                .city("PRESERVED-CITY")
                .build();

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder().build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(artisanRepository.findById("artisan-preserve-id")).thenReturn(Optional.of(existingArtisan));

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ArtisanResponseDTO.class);
        verify(artisanRepository).save(existingArtisan);
        assertThat(existingArtisan.getRegionId()).isEqualTo("PRESERVED-REG");
        assertThat(existingArtisan.getCity()).isEqualTo("PRESERVED-CITY");
    }

    /**
     * Verifies completeProfile preserves existing client regionId and city when DTO fields are null.
     */
    @Test
    @DisplayName("completeProfile: preserves existing client regionId and city when optional DTO fields are null")
    void completeProfile_forClient_withNullOptionalFields_preservesExistingValues() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-preserve-id");

        Client existingClient = Client.builder()
                .user(user)
                .regionId("PRESERVED-CLIENT-REG")
                .city("PRESERVED-CLIENT-CITY")
                .build();

        CompleteProfileRequestDTO dto = CompleteProfileRequestDTO.builder().build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(clientRepository.findById("client-preserve-id")).thenReturn(Optional.of(existingClient));

        ProfileResponse response = authService.completeProfile(dto);

        assertThat(response).isInstanceOf(ClientProfileResponseDTO.class);
        verify(clientRepository).save(existingClient);
        assertThat(existingClient.getRegionId()).isEqualTo("PRESERVED-CLIENT-REG");
        assertThat(existingClient.getCity()).isEqualTo("PRESERVED-CLIENT-CITY");
    }

    /**
     * Verifies patchCurrentUser leaves artisan region unchanged when payload contains no region keys.
     */
    @Test
    @DisplayName("patchCurrentUser: leaves artisan region unchanged when payload contains no region keys")
    void patchCurrentUser_forArtisan_withoutRegionFields_leavesRegionUnchanged() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        User user = User.builder()
                .email("artisan@example.com")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .build();
        user.setId("artisan-no-reg-patch");

        Artisan existingArtisan = Artisan.builder()
                .user(user)
                .regionId("ORIGINAL-REG")
                .bio("Original bio")
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Updated bio only");

        ArtisanPatchDTO patchDTO = ArtisanPatchDTO.builder()
                .bio("Updated bio only")
                .build();

        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ArtisanPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(artisanRepository.findById("artisan-no-reg-patch")).thenReturn(Optional.of(existingArtisan));

        authService.patchCurrentUser(payload);

        verify(artisanRepository).save(existingArtisan);
        assertThat(existingArtisan.getBio()).isEqualTo("Updated bio only");
        assertThat(existingArtisan.getRegionId()).isEqualTo("ORIGINAL-REG");
    }

    /**
     * Verifies patchCurrentUser leaves client region unchanged when payload contains no region keys.
     */
    @Test
    @DisplayName("patchCurrentUser: leaves client region unchanged when payload contains no region keys")
    void patchCurrentUser_forClient_withoutRegionFields_leavesRegionUnchanged() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("client@example.com", "cred",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        User user = User.builder()
                .email("client@example.com")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();
        user.setId("client-no-reg-patch");

        Client existingClient = Client.builder()
                .user(user)
                .regionId("ORIGINAL-CLIENT-REG")
                .bio("Original client bio")
                .build();

        ObjectNode payload = new JsonMapper().createObjectNode();
        payload.put("bio", "Updated client bio only");

        ClientPatchDTO patchDTO = ClientPatchDTO.builder()
                .bio("Updated client bio only")
                .build();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(jsonMapper.treeToValue(payload, ClientPatchDTO.class)).thenReturn(patchDTO);
        when(validator.validate(patchDTO)).thenReturn(Collections.emptySet());
        when(clientRepository.findById("client-no-reg-patch")).thenReturn(Optional.of(existingClient));

        authService.patchCurrentUser(payload);

        verify(clientRepository).save(existingClient);
        assertThat(existingClient.getBio()).isEqualTo("Updated client bio only");
        assertThat(existingClient.getRegionId()).isEqualTo("ORIGINAL-CLIENT-REG");
    }

    /**
     * Verifies processOAuth2Success falls back to getName when sub attribute is null.
     */
    @Test
    @DisplayName("processOAuth2Success: falls back to getName when sub attribute is null")
    void processOAuth2Success_whenSubAttributeNull_fallsBackToOAuthUserName() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn(null);
        when(oAuth2User.getName()).thenReturn("google-name-null-sub");
        when(oAuth2User.getAttribute("email")).thenReturn("existing@example.com");

        User existingUser = User.builder()
                .email("existing@example.com")
                .status(AccountStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        OAuthIdentity identity = OAuthIdentity.builder()
                .user(existingUser)
                .provider("GOOGLE")
                .providerUserId("google-name-null-sub")
                .build();

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-name-null-sub"))
                .thenReturn(Optional.of(identity));
        when(jwtUtils.generateAccessToken("existing@example.com")).thenReturn("access-token-oauth");
        when(refreshTokenService.createRefreshTokenForUser(existingUser))
                .thenReturn(RefreshToken.builder().token("refresh-token-oauth").build());

        JwtResponseDTO response = authService.processOAuth2Success(oAuth2User, null, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token-oauth");
    }

    /**
     * Verifies processOAuth2Success throws BadRequestException when email from OAuth provider is blank.
     */
    @Test
    @DisplayName("processOAuth2Success: throws BadRequestException when email from OAuth provider is blank")
    void processOAuth2Success_whenEmailBlankFromOAuthUser_throwsBadRequestException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-blank-email");
        when(oAuth2User.getAttribute("email")).thenReturn("   ");

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("OAuth provider did not return an email address.");
    }

    /**
     * Verifies processOAuth2Success throws BadRequestException when intentRole is blank.
     */
    @Test
    @DisplayName("processOAuth2Success: throws BadRequestException when intentRole is blank string")
    void processOAuth2Success_whenNewUserAndIntentRoleBlank_throwsBadRequestException() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-intent-blank");
        when(oAuth2User.getAttribute("email")).thenReturn("brandnew@example.com");

        when(oauthIdentityRepository.findByProviderAndProviderUserId("GOOGLE", "google-sub-intent-blank"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("brandnew@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.processOAuth2Success(oAuth2User, "   ", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("OAuth registration intent not found or expired.");
    }

    /**
     * Verifies mapToSummaryDTO correctly maps false flags for artisan when teacher, premium, and verified are false.
     */
    @Test
    @DisplayName("mapToSummaryDTO: maps all flags as false when artisan has no teacher/premium/verified status")
    void mapToSummaryDTO_withArtisanAllFlagsFalse_mapsExpectedSummary() {
        Artisan artisan = Artisan.builder()
                .isTeacher(false)
                .isPremium(false)
                .isVerified(false)
                .build();

        User user = User.builder()
                .email("plainartisan@example.com")
                .firstName("Hassan")
                .lastName("B")
                .roles(new HashSet<>(Set.of(artisanRole)))
                .artisan(artisan)
                .build();

        UserSummaryDTO summary = authService.mapToSummaryDTO(user);

        assertThat(summary.isTeacher()).isFalse();
        assertThat(summary.isPremium()).isFalse();
        assertThat(summary.isValidated()).isFalse();
    }

    /**
     * Verifies mapToSummaryDTO correctly maps false flags for client when premium and verified are false.
     */
    @Test
    @DisplayName("mapToSummaryDTO: maps all flags as false when client has no premium/verified status")
    void mapToSummaryDTO_withClientAllFlagsFalse_mapsExpectedSummary() {
        Client client = Client.builder()
                .isPremium(false)
                .isVerified(false)
                .build();

        User user = User.builder()
                .email("plainclient@example.com")
                .firstName("Mona")
                .lastName("S")
                .roles(new HashSet<>(Set.of(clientRole)))
                .client(client)
                .build();

        UserSummaryDTO summary = authService.mapToSummaryDTO(user);

        assertThat(summary.isTeacher()).isFalse();
        assertThat(summary.isPremium()).isFalse();
        assertThat(summary.isValidated()).isFalse();
    }
}
