package com.project.souklab.service.feed;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.feed.FeedPostCreateDTO;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.lifecycle.StorageObjectLifecycle;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.model.User;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.security.AccessControlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies feed author eligibility and pending moderation creation.
 */
@ExtendWith(MockitoExtension.class)
class FeedPostServiceTest {

    @Mock private FeedPostRepository postRepository;
    @Mock private FormationRepository formationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ArtisanRepository artisanRepository;
    @Mock private FileValidator fileValidator;
    @Mock private VirusScanService virusScanService;
    @Mock private StorageService storageService;
    @Mock private FileUrlResolver fileUrlResolver;
    @Mock private NotificationService notificationService;
    @Mock private StorageObjectLifecycle storageObjectLifecycle;
    @Mock private AccessControlService accessControlService;

    @InjectMocks private FeedPostService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().email("artisan@example.com").status(AccountStatus.ACTIVE).build();
        user.setId("user-1");
        Artisan artisan = Artisan.builder().id("artisan-1").user(user).isVerified(true).build();
        user.setArtisan(artisan);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "artisan@example.com", "credentials", List.of(new SimpleGrantedAuthority("permission:artisan:content"))));
        when(userRepository.findByEmail("artisan@example.com")).thenReturn(Optional.of(user));
        when(accessControlService.isAdmin(any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifiedArtisanCreatesPendingPost() {
        when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        FeedPostCreateDTO request = new FeedPostCreateDTO(FeedPostType.ACTUALITE, "Title", "Body", null);

        var response = service.create(request);

        assertThat(response.getStatus()).isEqualTo(FeedPostStatus.PENDING.name());
        assertThat(response.getTitle()).isEqualTo("Title");
    }

    @Test
    void unverifiedArtisanIsRejected() {
        user.getArtisan().setVerified(false);
        FeedPostCreateDTO request = new FeedPostCreateDTO(FeedPostType.ACTUALITE, "Title", "Body", null);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ForbiddenException.class);
    }
}
