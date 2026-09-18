package com.project.souklab.config;

import com.project.souklab.dao.AuthorizationPermissionRepository;
import com.project.souklab.dao.EpoqueRepository;
import com.project.souklab.dao.JobCategoryRepository;
import com.project.souklab.dao.JobSubCategoryRepository;
import com.project.souklab.dao.MaterialFamilyRepository;
import com.project.souklab.dao.MaterialRepository;
import com.project.souklab.dao.RegionRepository;
import com.project.souklab.dao.TechniqueRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.security.Permission;
import com.project.souklab.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthorizationPermissionRepository permissionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailUtil emailUtil;
    @Mock private RegionRepository regionRepository;
    @Mock private JobCategoryRepository jobCategoryRepository;
    @Mock private JobSubCategoryRepository jobSubCategoryRepository;
    @Mock private MaterialFamilyRepository materialFamilyRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private EpoqueRepository epoqueRepository;
    @Mock private TechniqueRepository techniqueRepository;

    private AppProperties appProperties;
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getAdmin().setBootstrapEnabled(true);
        appProperties.getAdmin().setDefaultEmail(" Admin@Example.COM ");
        appProperties.getAdmin().setDefaultPassword("initial-password");
        dataSeeder = new DataSeeder(userRepository, permissionRepository, passwordEncoder,
                appProperties, emailUtil, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                regionRepository, jobCategoryRepository, jobSubCategoryRepository,
                materialFamilyRepository, materialRepository, epoqueRepository, techniqueRepository);

        AuthorizationPermission permission = new AuthorizationPermission();
        permission.setPermissionKey(Permission.PROFILE_READ.authority());
        when(permissionRepository.findByPermissionKeyAndEnabledTrue(anyString())).thenReturn(Optional.of(permission));
        lenient().when(regionRepository.count()).thenReturn(1L);
        lenient().when(jobCategoryRepository.count()).thenReturn(1L);
        lenient().when(materialFamilyRepository.count()).thenReturn(1L);
        lenient().when(epoqueRepository.count()).thenReturn(1L);
        lenient().when(techniqueRepository.count()).thenReturn(1L);
    }

    @Test
    void bootstrapsMissingAdministratorAndSendsWelcomeNotification() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(permissionRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode("initial-password")).thenReturn("encoded-password");

        dataSeeder.run();

        verify(userRepository).save(any());
        verify(emailUtil).sendAdminWelcomeEmail("admin@example.com", "initial-password");
    }

    @Test
    void doesNotRecreateExistingAdministrator() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        dataSeeder.run();

        verify(userRepository, never()).save(any());
        verify(emailUtil, never()).sendAdminWelcomeEmail(anyString(), anyString());
    }

    @Test
    void rejectsMissingAdministratorCredentials() {
        appProperties.getAdmin().setDefaultEmail(" ");
        assertThatThrownBy(() -> dataSeeder.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_ADMIN_DEFAULT_EMAIL");

        appProperties.getAdmin().setDefaultEmail("admin@example.com");
        appProperties.getAdmin().setDefaultPassword(null);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        assertThatThrownBy(() -> dataSeeder.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_ADMIN_DEFAULT_PASSWORD");
    }

    @Test
    void continuesWhenAdministratorWelcomeEmailFails() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(permissionRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode("initial-password")).thenReturn("encoded-password");
        doThrow(new IllegalStateException("mail unavailable"))
                .when(emailUtil).sendAdminWelcomeEmail("admin@example.com", "initial-password");

        dataSeeder.run();

        verify(userRepository).save(any());
    }
}
