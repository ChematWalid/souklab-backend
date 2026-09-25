package com.project.souklab.service.feed;

import com.project.souklab.dto.feed.FeedPostCommentResponseDTO;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostComment;
import com.project.souklab.model.User;
import com.project.souklab.security.ViewerPremiumResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedPrivacyServiceTest {

    @Mock
    private ViewerPremiumResolver viewerPremiumResolver;

    @Test
    void masksArtisanPostAuthorForLockedViewer() {
        FeedPrivacyService service = new FeedPrivacyService(viewerPremiumResolver);
        User author = artisanUser("user-1", "artisan-12345");
        FeedPost post = FeedPost.builder().author(author).build();
        FeedPostResponseDTO response = FeedPostResponseDTO.builder().authorName("Real Artisan").build();
        when(viewerPremiumResolver.isContactInfoLockedFor(author)).thenReturn(true);

        assertThat(service.protectPost(post, response).getAuthorName()).isEqualTo("Artisan #12345");
    }

    @Test
    void preservesArtisanCommentAuthorForPremiumViewer() {
        FeedPrivacyService service = new FeedPrivacyService(viewerPremiumResolver);
        User author = artisanUser("user-1", "artisan-12345");
        FeedPostComment comment = FeedPostComment.builder().user(author).build();
        FeedPostCommentResponseDTO response = FeedPostCommentResponseDTO.builder().authorName("Real Artisan").build();
        when(viewerPremiumResolver.isContactInfoLockedFor(author)).thenReturn(false);

        assertThat(service.protectComment(comment, response).getAuthorName()).isEqualTo("Real Artisan");
    }

    @Test
    void doesNotMaskClientCommentAuthor() {
        FeedPrivacyService service = new FeedPrivacyService(viewerPremiumResolver);
        User author = User.builder().email("client@test.com").build();
        FeedPostComment comment = FeedPostComment.builder().user(author).build();
        FeedPostCommentResponseDTO response = FeedPostCommentResponseDTO.builder().authorName("Client Name").build();

        assertThat(service.protectComment(comment, response).getAuthorName()).isEqualTo("Client Name");
    }

    private User artisanUser(String userId, String artisanId) {
        User user = User.builder().email("artisan@test.com").build();
        user.setId(userId);
        user.setArtisan(Artisan.builder().id(artisanId).user(user).build());
        return user;
    }
}
