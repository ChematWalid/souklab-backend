package com.project.souklab.dto.admin;

import com.project.souklab.security.Permission;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @JsonProperty("permissionKey")
    private Permission permission;
}
