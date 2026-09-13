package com.project.souklab.service.artisan;

import com.project.souklab.dao.ArtisanCertificationRepository;
import com.project.souklab.dao.ArtisanGalleryImageRepository;
import com.project.souklab.dao.ArtisanProfileViewRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.profile.ArtisanPublicViewDTO;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanCertification;
import com.project.souklab.model.ArtisanGalleryImage;
import com.project.souklab.model.ArtisanProfileView;
import com.project.souklab.model.Client;
import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.Region;
import com.project.souklab.model.Role;
import com.project.souklab.model.Technique;
import com.project.souklab.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit test suite for ArtisanProfileService covering authentication,
 * status and email verification gating, view count tracking, privacy contact masking,
 * taxonomy summary assembly, showcase gallery images, and official certifications.
 */
@ExtendWith(MockitoExtension.class)
class ArtisanProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtisanRepository artisanRepository;

    @Mock
    private ArtisanProfileViewRepository artisanProfileViewRepository;

    @Mock
    private ArtisanGalleryImageRepository artisanGalleryImageRepository;

    @Mock
    private ArtisanCertificationRepository artisanCertificationRepository;

    @InjectMocks
    private ArtisanProfileService artisanProfileService;

    private User targetUser;
    private Artisan targetArtisan;
    private Region region;
    private JobCategory category;
    private JobSubCategory subCategory;
    private Material material;
    private Technique technique;
    private Epoque epoque;

    /**
     * Creates a test Role entity with the specified name.
     */
    private Role createRole(String name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }

    /**
     * Initializes test entities and security context preconditions before each test execution.
     */
    @BeforeEach
    void setUp() {
        region = Region.builder()
                .name("Tizi Ouzou")
                .slug("tizi-ouzou")
                .code("15")
                .build();
        region.setId("reg-15");

        category = JobCategory.builder()
                .name("Jewelry & Metalsmithing")
                .slug("jewelry-metalsmithing")
                .build();
        category.setId("cat-1");

        subCategory = JobSubCategory.builder()
                .name("Kabyle Silver Jewelry")
                .slug("kabyle-silver-jewelry")
                .category(category)
                .build();
        subCategory.setId("subcat-10");

        material = Material.builder()
                .name("Coral")
                .slug("coral")
                .build();
        material.setId("mat-coral");

        technique = Technique.builder()
                .name("Cloisonné Enameling")
                .slug("cloisonne-enameling")
                .build();
        technique.setId("tech-enamel");

        epoque = Epoque.builder()
                .name("Kabyle Heritage")
                .slug("kabyle-heritage")
                .periodEra("Traditional")
                .build();
        epoque.setId("epo-kabyle");

        targetUser = User.builder()
                .email("artisan@souklab.dz")
                .firstName("Djamel")
                .lastName("Beni Yenni")
                .phone("+213550123456")
                .avatarUrl("https://storage.souklab.dz/avatars/djamel.jpg")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();
        targetUser.setId("artisan-user-id");

        targetArtisan = Artisan.builder()
                .user(targetUser)
                .bio("Master silver craftsman from Beni Yenni")
                .city("Beni Yenni")
                .region(region)
                .subCategory(subCategory)
                .materials(Set.of(material))
                .techniques(Set.of(technique))
                .epoques(Set.of(epoque))
                .website("https://djamel-bijoux.dz")
                .address("Village Taourirt Mimoun")
                .rating(4.9)
                .reviewsCount(58)
                .viewsCount(120)
                .isTeacher(true)
                .isVerified(true)
                .build();
        targetArtisan.setId("artisan-user-id");
    }

    /**
     * Clears authentication from SecurityContextHolder after each test execution.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to authenticate a viewer with given email and roles.
     */
    private void setAuthenticatedViewer(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "cred", authorities)
        );
    }

    /**
     * Verifies getArtisanProfile throws UnauthorizedException when no authenticated principal exists.
     */
    @Test
    @DisplayName("getArtisanProfile: throws UnauthorizedException when unauthenticated")
    void getArtisanProfile_whenUnauthenticated_throwsUnauthorizedException() {
        assertThatThrownBy(() -> artisanProfileService.getArtisanProfile("artisan-user-id"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Not authenticated.");
    }

    /**
     * Verifies getArtisanProfile throws ResourceNotFoundException when authenticated viewer is not in DB.
     */
    @Test
    @DisplayName("getArtisanProfile: throws ResourceNotFoundException when viewer user not found")
    void getArtisanProfile_whenViewerNotFound_throwsResourceNotFoundException() {
        setAuthenticatedViewer("viewer@souklab.dz", "ROLE_CLIENT");
        when(userRepository.findByEmail("viewer@souklab.dz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artisanProfileService.getArtisanProfile("artisan-user-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: viewer@souklab.dz");
    }

    /**
     * Verifies getArtisanProfile throws ForbiddenException when non-admin viewer status is not ACTIVE.
     */
    @Test
    @DisplayName("getArtisanProfile: throws ForbiddenException when non-admin viewer is not active")
    void getArtisanProfile_whenViewerNotActive_throwsForbiddenException() {
        setAuthenticatedViewer("viewer@souklab.dz", "ROLE_CLIENT");
        User viewer = User.builder()
                .email("viewer@souklab.dz")
                .status(AccountStatus.PENDING)
                .emailVerified(true)
                .roles(Set.of(createRole("ROLE_CLIENT")))
                .build();
        when(userRepository.findByEmail("viewer@souklab.dz")).thenReturn(Optional.of(viewer));

        assertThatThrownBy(() -> artisanProfileService.getArtisanProfile("artisan-user-id"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Your account is not active.");
    }

    /**
     * Verifies getArtisanProfile throws ForbiddenException when non-admin viewer email is not verified.
     */
    @Test
    @DisplayName("getArtisanProfile: throws ForbiddenException when non-admin viewer email is unverified")
    void getArtisanProfile_whenViewerEmailNotVerified_throwsForbiddenException() {
        setAuthenticatedViewer("viewer@souklab.dz", "ROLE_CLIENT");
        User viewer = User.builder()
                .email("viewer@souklab.dz")
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .roles(Set.of(createRole("ROLE_CLIENT")))
                .build();
        when(userRepository.findByEmail("viewer@souklab.dz")).thenReturn(Optional.of(viewer));

        assertThatThrownBy(() -> artisanProfileService.getArtisanProfile("artisan-user-id"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Please verify your email address to access artisan profiles.");
    }

    /**
     * Verifies getArtisanProfile throws ResourceNotFoundException when target artisan ID does not exist.
     */
    @Test
    @DisplayName("getArtisanProfile: throws ResourceNotFoundException when target artisan does not exist")
    void getArtisanProfile_whenArtisanNotFound_throwsResourceNotFoundException() {
        setAuthenticatedViewer("admin@souklab.dz", "ROLE_ADMIN");
        User admin = User.builder()
                .email("admin@souklab.dz")
                .status(AccountStatus.ACTIVE)
                .roles(Set.of(createRole("ROLE_ADMIN")))
                .build();
        when(userRepository.findByEmail("admin@souklab.dz")).thenReturn(Optional.of(admin));
        when(artisanRepository.findById("missing-artisan")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artisanProfileService.getArtisanProfile("missing-artisan"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artisan not found with id: missing-artisan");
    }

    /**
     * Verifies getArtisanProfile unmasks contact info for self-viewer and skips view count recording.
     */
    @Test
    @DisplayName("getArtisanProfile: self-viewer sees unmasked contact info without incrementing views")
    void getArtisanProfile_whenViewerIsSelf_unmasksContactInfoAndDoesNotIncrementViewCount() {
        setAuthenticatedViewer("artisan@souklab.dz", "ROLE_ARTISAN");
        targetUser.setRoles(Set.of(createRole("ROLE_ARTISAN")));

        when(userRepository.findByEmail("artisan@souklab.dz")).thenReturn(Optional.of(targetUser));
        when(artisanRepository.findById("artisan-user-id")).thenReturn(Optional.of(targetArtisan));
        when(artisanGalleryImageRepository.findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc("artisan-user-id"))
                .thenReturn(Collections.emptyList());
        when(artisanCertificationRepository.findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc("artisan-user-id"))
                .thenReturn(Collections.emptyList());

        ArtisanPublicViewDTO result = artisanProfileService.getArtisanProfile("artisan-user-id");

        assertThat(result.isContactInfoLocked()).isFalse();
        assertThat(result.getName()).isEqualTo("Djamel Beni Yenni");
        assertThat(result.getPhone()).isEqualTo("+213550123456");
        assertThat(result.getEmail()).isEqualTo("artisan@souklab.dz");
        assertThat(result.getWebsite()).isEqualTo("https://djamel-bijoux.dz");
        assertThat(result.getAddress()).isEqualTo("Village Taourirt Mimoun");

        verify(artisanProfileViewRepository, never()).save(any(ArtisanProfileView.class));
        verify(artisanRepository, never()).save(any(Artisan.class));
    }

    /**
     * Verifies getArtisanProfile unmasks contact info for admin viewer and skips view count recording.
     */
    @Test
    @DisplayName("getArtisanProfile: admin viewer sees unmasked contact info without incrementing views")
    void getArtisanProfile_whenViewerIsAdmin_unmasksContactInfoAndDoesNotIncrementViewCount() {
        setAuthenticatedViewer("admin@souklab.dz", "ROLE_ADMIN");
        User admin = User.builder()
                .email("admin@souklab.dz")
                .status(AccountStatus.SUSPENDED)
                .emailVerified(false)
                .roles(Set.of(createRole("ROLE_ADMIN")))
                .build();
        admin.setId("admin-id");

        when(userRepository.findByEmail("admin@souklab.dz")).thenReturn(Optional.of(admin));
        when(artisanRepository.findById("artisan-user-id")).thenReturn(Optional.of(targetArtisan));
        when(artisanGalleryImageRepository.findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc("artisan-user-id"))
                .thenReturn(Collections.emptyList());
        when(artisanCertificationRepository.findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc("artisan-user-id"))
                .thenReturn(Collections.emptyList());

        ArtisanPublicViewDTO result = artisanProfileService.getArtisanProfile("artisan-user-id");

        assertThat(result.isContactInfoLocked()).isFalse();
        assertThat(result.getName()).isEqualTo("Djamel Beni Yenni");
        verify(artisanProfileViewRepository, never()).save(any(ArtisanProfileView.class));
    }

    /**
     * Verifies getArtisanProfile masks contact info and hides certification documentUrl for non-premium viewer.
     */
    @Test
    @DisplayName("getArtisanProfile: non-premium viewer receives masked contact info and masked certification URLs")
    void getArtisanProfile_whenViewerIsNonPremium_masksContactInfoAndNullsDocumentUrl() {
        setAuthenticatedViewer("client@souklab.dz", "ROLE_CLIENT");
        Client client = Client.builder().isPremium(false).build();
        User viewer = User.builder()
                .email("client@souklab.dz")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .client(client)
                .roles(Set.of(createRole("ROLE_CLIENT")))
                .build();
        viewer.setId("client-viewer-id");

        ArtisanGalleryImage image = ArtisanGalleryImage.builder()
                .imageUrl("https://storage.souklab.dz/showcase/bracelet.jpg")
                .title("Kabyle Silver Bracelet")
                .caption("Handcrafted silver with coral cabochons")
                .displayOrder(1)
                .build();
        image.setId("img-1");

        ArtisanCertification cert = ArtisanCertification.builder()
                .title("CAM Master Craftsman Accreditation")
                .issuer("Chambre de l'Artisanat et des Métiers de Tizi Ouzou")
                .issuedAt(LocalDate.of(2023, 5, 10))
                .isVerified(true)
                .documentUrl("https://storage.souklab.dz/private/cam-card.pdf")
                .build();
        cert.setId("cert-1");

        when(userRepository.findByEmail("client@souklab.dz")).thenReturn(Optional.of(viewer));
        when(artisanRepository.findById("artisan-user-id")).thenReturn(Optional.of(targetArtisan));
        when(artisanProfileViewRepository.existsByViewerIdAndArtisanId("client-viewer-id", "artisan-user-id"))
                .thenReturn(false);
        when(artisanGalleryImageRepository.findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc("artisan-user-id"))
                .thenReturn(List.of(image));
        when(artisanCertificationRepository.findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc("artisan-user-id"))
                .thenReturn(List.of(cert));

        ArtisanPublicViewDTO result = artisanProfileService.getArtisanProfile("artisan-user-id");

        assertThat(result.isContactInfoLocked()).isTrue();
        assertThat(result.getName()).startsWith("Artisan #");
        assertThat(result.getPhone()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getWebsite()).isNull();
        assertThat(result.getAddress()).isNull();

        assertThat(result.getRegion()).isNotNull();
        assertThat(result.getRegion().getName()).isEqualTo("Tizi Ouzou");
        assertThat(result.getSubCategory().getName()).isEqualTo("Kabyle Silver Jewelry");
        assertThat(result.getMaterials()).hasSize(1);
        assertThat(result.getTechniques()).hasSize(1);
        assertThat(result.getEpoques()).hasSize(1);

        assertThat(result.getGalleryImages()).hasSize(1);
        assertThat(result.getGalleryImages().get(0).getImageUrl()).isEqualTo("https://storage.souklab.dz/showcase/bracelet.jpg");

        assertThat(result.getCertifications()).hasSize(1);
        assertThat(result.getCertifications().get(0).isVerified()).isTrue();
        assertThat(result.getCertifications().get(0).getDocumentUrl()).isNull();

        verify(artisanProfileViewRepository).save(any(ArtisanProfileView.class));
        verify(artisanRepository).save(targetArtisan);
        assertThat(targetArtisan.getViewsCount()).isEqualTo(121);
    }

    /**
     * Verifies getArtisanProfile unmasks full contact info and documentUrl for premium client viewer.
     */
    @Test
    @DisplayName("getArtisanProfile: premium client viewer receives unmasked contact info and certification URLs")
    void getArtisanProfile_whenViewerIsPremiumClient_unmasksAllFields() {
        setAuthenticatedViewer("premium@souklab.dz", "ROLE_CLIENT");
        Client client = Client.builder().isPremium(true).build();
        User viewer = User.builder()
                .email("premium@souklab.dz")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .client(client)
                .roles(Set.of(createRole("ROLE_CLIENT")))
                .build();
        viewer.setId("premium-viewer-id");

        ArtisanCertification cert = ArtisanCertification.builder()
                .title("CAM Master Craftsman Accreditation")
                .issuer("CAM Tizi Ouzou")
                .issuedAt(LocalDate.of(2023, 5, 10))
                .isVerified(true)
                .documentUrl("https://storage.souklab.dz/private/cam-card.pdf")
                .build();
        cert.setId("cert-1");

        when(userRepository.findByEmail("premium@souklab.dz")).thenReturn(Optional.of(viewer));
        when(artisanRepository.findById("artisan-user-id")).thenReturn(Optional.of(targetArtisan));
        when(artisanProfileViewRepository.existsByViewerIdAndArtisanId("premium-viewer-id", "artisan-user-id"))
                .thenReturn(true);
        when(artisanGalleryImageRepository.findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc("artisan-user-id"))
                .thenReturn(Collections.emptyList());
        when(artisanCertificationRepository.findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc("artisan-user-id"))
                .thenReturn(List.of(cert));

        ArtisanPublicViewDTO result = artisanProfileService.getArtisanProfile("artisan-user-id");

        assertThat(result.isContactInfoLocked()).isFalse();
        assertThat(result.getName()).isEqualTo("Djamel Beni Yenni");
        assertThat(result.getPhone()).isEqualTo("+213550123456");
        assertThat(result.getEmail()).isEqualTo("artisan@souklab.dz");
        assertThat(result.getWebsite()).isEqualTo("https://djamel-bijoux.dz");
        assertThat(result.getAddress()).isEqualTo("Village Taourirt Mimoun");

        assertThat(result.getCertifications()).hasSize(1);
        assertThat(result.getCertifications().get(0).getDocumentUrl()).isEqualTo("https://storage.souklab.dz/private/cam-card.pdf");

        verify(artisanProfileViewRepository, never()).save(any(ArtisanProfileView.class));
        verify(artisanRepository, never()).save(any(Artisan.class));
        assertThat(targetArtisan.getViewsCount()).isEqualTo(120);
    }
}
