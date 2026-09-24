package com.project.souklab.dto.auth;

import com.project.souklab.validation.DifferentPasswords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DifferentPasswords
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Current password is required")
    @Size(max = 128, message = "Current password must not exceed 128 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    @Size(max = 128, message = "New password must not exceed 128 characters")
    private String newPassword;
}

