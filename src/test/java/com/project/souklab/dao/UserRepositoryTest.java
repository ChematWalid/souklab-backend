package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies user persistence and direct permission loading.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailLoadsDirectPermissions() {
        AuthorizationPermission permission = new AuthorizationPermission("permission:profile:read", "Read profiles", true);
        entityManager.persist(permission);
        User user = User.builder().email("user@souklab.com").status(AccountStatus.ACTIVE)
                .permissions(Set.of(permission)).build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findByEmail("user@souklab.com").orElseThrow();

        assertThat(found.getPermissions()).extracting(AuthorizationPermission::getPermissionKey)
                .containsExactly("permission:profile:read");
    }

    @Test
    void findByPermissionKeyReturnsEnabledUsersOnly() {
        AuthorizationPermission permission = new AuthorizationPermission("permission:admin:users", "Manage users", true);
        entityManager.persist(permission);
        entityManager.persist(User.builder().email("admin@souklab.com").status(AccountStatus.ACTIVE)
                .permissions(Set.of(permission)).build());
        entityManager.flush();

        assertThat(userRepository.findByPermissionKey("permission:admin:users")).hasSize(1);
    }
}
