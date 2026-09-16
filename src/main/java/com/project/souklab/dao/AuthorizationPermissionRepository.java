package com.project.souklab.dao;

import com.project.souklab.model.AuthorizationPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence access for canonical authorization permissions.
 */
public interface AuthorizationPermissionRepository extends JpaRepository<AuthorizationPermission, String> {

    Optional<AuthorizationPermission> findByPermissionKeyAndEnabledTrue(String permissionKey);

    List<AuthorizationPermission> findByPermissionKeyInAndEnabledTrue(Collection<String> permissionKeys);
}
