package com.project.souklab.dto.auth;

import com.project.souklab.model.AccountRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Size(max = 128, message = "Password must not exceed 128 characters")
    private String password;

    private String name;

    private String firstName;

    private String lastName;

    @NotNull(message = "Account type is required (ARTISAN or CLIENT)")
    private AccountRole accountType;
}
