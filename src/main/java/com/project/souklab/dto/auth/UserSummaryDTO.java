package com.project.souklab.dto.auth;

import com.project.souklab.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private Set<String> permissions;
    private AccountStatus accountStatus;
    private boolean isPremium;
    private boolean isValidated;
    private boolean isTeacher;
}
