package com.project.souklab.controller.auth;

import com.project.souklab.dto.auth.ChangePasswordRequestDTO;
import com.project.souklab.dto.auth.CompleteProfileRequestDTO;
import com.project.souklab.dto.auth.ForgotPasswordRequestDTO;
import com.project.souklab.dto.auth.JwtResponseDTO;
import com.project.souklab.dto.auth.LoginDTO;
import com.project.souklab.dto.auth.ResendVerificationRequestDTO;
import com.project.souklab.dto.auth.ResetPasswordRequestDTO;
import com.project.souklab.dto.auth.TokenRefreshRequestDTO;
import com.project.souklab.dto.auth.UserRegistrationDTO;
import com.project.souklab.dto.auth.VerifyEmailRequestDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.profile.ProfileResponse;
import com.project.souklab.dto.profile.UserPatchDTO;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.service.auth.AuthService;
import com.project.souklab.service.profile.ProfileService;
import com.project.souklab.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * HTTP adapter for authentication and profile endpoints.
 * Authentication operations (login, register, tokens, passwords, OAuth2) delegate to {@link AuthService}.
 * Profile lifecycle operations (get/patch profile, complete profile) delegate to {@link ProfileService}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    /**
     * Registers a new user and returns the role-specific profile response.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProfileResponse>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        ProfileResponse response = authService.registerUser(registrationDTO);
        boolean isPending = response.getAccountStatus() == AccountStatus.PENDING;
        String message = isPending
                ? "Registration successful. Your artisan account has been created and is pending administrator verification."
                : "Registration successful. Welcome to Souklab!";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, message));
    }

    /**
     * Authenticates a user and returns JWT access and refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        JwtResponseDTO response = authService.login(loginDTO, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    /**
     * Rotates the provided refresh token and returns a new JWT pair.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        JwtResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }

    /**
     * Revokes tokens for the current user and logs them out.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) TokenRefreshRequestDTO tokenRequest,
            HttpServletRequest request) {
        String userEmail = SecurityUtils.getCurrentUsername();
        String refreshToken = tokenRequest != null ? tokenRequest.getRefreshToken() : null;
        authService.logout(userEmail, refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful."));
    }

    /**
     * Verifies the submitted email verification code.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully."));
    }

    /**
     * Resends the email verification code to the given address if the account exists and is unverified.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequestDTO request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If an unverified account exists for this email, a verification code has been sent."));
    }

    /**
     * Initiates the password reset flow for the given email address.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If an account exists for this email, instructions have been sent."));
    }

    /**
     * Resets the password using the 6-digit code sent via email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. You can now log in with your new password."));
    }

    /**
     * Changes the authenticated user's password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }

    /**
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(profileService.getCurrentUser()));
    }

    /**
     * Partially updates the authenticated user's profile using a strongly-typed patch DTO.
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> patchCurrentUser(@Valid @RequestBody(required = false) UserPatchDTO patchDTO) {
        ProfileResponse response = profileService.patchCurrentUser(patchDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully."));
    }

    /**
     * Completes the artisan or client profile for the authenticated user.
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> completeProfile(@Valid @RequestBody CompleteProfileRequestDTO request) {
        ProfileResponse response = profileService.completeProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile completed successfully."));
    }

    /**
     * Initiates the Google OAuth2 flow with artisan registration intent.
     */
    @GetMapping("/oauth/google/artisan")
    public void initiateGoogleOAuthArtisan(HttpServletResponse response) throws IOException {
        setIntentCookie(response, "ROLE_ARTISAN");
        response.sendRedirect("/oauth2/authorization/google");
    }

    /**
     * Initiates the Google OAuth2 flow with client registration intent.
     */
    @GetMapping("/oauth/google/client")
    public void initiateGoogleOAuthClient(HttpServletResponse response) throws IOException {
        setIntentCookie(response, "ROLE_CLIENT");
        response.sendRedirect("/oauth2/authorization/google");
    }

    /**
     * Sets the OAuth2 registration intent cookie on the outgoing response.
     *
     * @param response   the HTTP response to attach the cookie to
     * @param intentRole the role intent string (e.g., {@code "ROLE_ARTISAN"})
     */
    private void setIntentCookie(HttpServletResponse response, String intentRole) {
        ResponseCookie cookie = ResponseCookie.from("SOUKLAB_OAUTH_INTENT", intentRole)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(300)

                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
