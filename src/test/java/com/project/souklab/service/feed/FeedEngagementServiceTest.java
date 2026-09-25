package com.project.souklab.service.feed;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.FeedPostBookmarkRepository;
import com.project.souklab.dao.FeedPostCommentLikeRepository;
import com.project.souklab.dao.FeedPostCommentRepository;
import com.project.souklab.dao.FeedPostLikeRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.feed.FeedPostCommentCreateDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostLike;
import com.project.souklab.model.FeedPostComment;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.model.User;
import com.project.souklab.security.AccessControlService;
import com.project.souklab.service.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.List;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FeedEngagementServiceTest {
    @Mock FeedPostRepository postRepository;
    @Mock FeedPostLikeRepository postLikeRepository;
    @Mock FeedPostBookmarkRepository bookmarkRepository;
    @Mock FeedPostCommentRepository commentRepository;
    @Mock FeedPostCommentLikeRepository commentLikeRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @Mock AccessControlService accessControlService;
    @Mock AppProperties appProperties;

    private FeedEngagementService service;
    private User user;
    private FeedPost post;

    @BeforeEach
    void setUp() {
        service = new FeedEngagementService(postRepository, postLikeRepository, bookmarkRepository,
                commentRepository, commentLikeRepository, userRepository, notificationService,
                appProperties, accessControlService,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        user = User.builder().email("user@test.com").status(AccountStatus.ACTIVE).build();
        user.setId("user-1");
        post = FeedPost.builder().author(user).type(FeedPostType.ACTUALITE)
                .title("title").body("body").status(FeedPostStatus.PUBLISHED).build();
        post.setId("post-1");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@test.com", "credentials", List.of()));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        lenient().when(postRepository.findByIdAndDeletedAtIsNull("post-1")).thenReturn(Optional.of(post));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void duplicatePostLikeIsIdempotentAndUnlikeIsIdempotent() {
        authenticate();
        when(postLikeRepository.existsByPostIdAndUserId("post-1", "user-1")).thenReturn(false, true);

        service.likePost("post-1");
        verify(postRepository).incrementLikeCount("post-1");

        assertThat(service.likePost("post-1").isLikedByCurrentUser()).isTrue();
        when(postLikeRepository.findByPostIdAndUserId("post-1", "user-1"))
                .thenReturn(Optional.of(FeedPostLike.builder().post(post).user(user).build()), Optional.empty());
        service.unlikePost("post-1");
        service.unlikePost("post-1");
        verify(postRepository).decrementLikeCount("post-1");
    }

    @Test
    void replyToReplyIsRejected() {
        authenticate();
        FeedPostComment root = FeedPostComment.builder().post(post).user(user).content("root").build();
        root.setId("root-1");
        FeedPostComment parent = FeedPostComment.builder().post(post).user(user)
                .parent(root).content("reply").build();
        parent.setId("reply-1");
        when(commentRepository.findByIdAndDeletedAtIsNull("reply-1")).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> service.reply("reply-1", new FeedPostCommentCreateDTO("nested")))
                .isInstanceOf(BadRequestException.class);
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@test.com", "credentials", List.of()));
    }
}
