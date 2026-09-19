package com.project.souklab.dto.auth;

import com.project.souklab.dto.profile.ProfileResponse;
import com.project.souklab.security.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresIn;
    /** Account-type profile object: ClientProfileResponseDTO for clients, ArtisanResponseDTO for artisans. */
    private ProfileResponse user;
    private List<Permission> permissions;
}
