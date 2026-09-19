package com.project.souklab.dao;

import com.project.souklab.model.OAuthIdentity;
import com.project.souklab.model.OAuthProvider;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, String> {
    Optional<OAuthIdentity> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    List<OAuthIdentity> findByUser(User user);
    Optional<OAuthIdentity> findByProviderAndEmail(OAuthProvider provider, String email);
}
