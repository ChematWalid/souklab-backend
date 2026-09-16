package com.project.souklab.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to grant or revoke one canonical permission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAssignmentRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String permissionKey;
}
