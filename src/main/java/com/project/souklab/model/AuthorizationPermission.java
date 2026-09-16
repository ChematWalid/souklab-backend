package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persisted permission definition used by the authorization subsystem.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationPermission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String permissionKey;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;
}
