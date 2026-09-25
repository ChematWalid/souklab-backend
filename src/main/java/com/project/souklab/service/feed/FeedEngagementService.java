package com.project.souklab.service.feed;

import com.project.souklab.dao.FeedPostBookmarkRepository;
import com.project.souklab.dao.FeedPostCommentLikeRepository;
import com.project.souklab.dao.FeedPostCommentRepository;
import com.project.souklab.dao.FeedPostLikeRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.feed.FeedPostCommentCreateDTO;
import com.project.souklab.dto.feed.FeedPostCommentResponseDTO;
import com.project.souklab.dto.feed.FeedPostLikeStatusDTO;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.dto.feed.FeedShareResponseDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostBookmark;
import com.project.souklab.model.FeedPostComment;
import com.project.souklab.model.FeedPostCommentLike;
import com.project.souklab.model.FeedPostLike;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.security.AccessControlService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class FeedEngagementService {
    private final FeedPostRepository postRepository;
    private final FeedPostLikeRepository postLikeRepository;
    private final FeedPostBookmarkRepository bookmarkRepository;
    private final FeedPostCommentRepository commentRepository;
    private final FeedPostCommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AppProperties appProperties;
    private final AccessControlService accessControlService;
    private final Clock clock;

    @Transactional
    public FeedPostLikeStatusDTO likePost(String postId) {
        User user = currentUser();
        FeedPost post = publishedPost(postId);
        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return likeStatus(postId, user, post.getLikeCount());
        }
        try {
            postLikeRepository.saveAndFlush(FeedPostLike.builder().post(post).user(user).build());
        } catch (DataIntegrityViolationException exception) {
            return likeStatus(postId, user, post.getLikeCount());
        }
        postRepository.incrementLikeCount(postId);
        int likeCount = Math.toIntExact(postLikeRepository.countByPostId(postId));
        if (!post.getAuthor().getId().equals(user.getId())) {
            notificationService.createOrUpdateAggregatedNotification(post.getAuthor(), NotificationType.Feed.POST_LIKED,
                    postId, "liked your post", likeCount, user.getName());
        }
        return likeStatus(postId, user, likeCount);
    }

    @Transactional
    public void unlikePost(String postId) {
        User user = currentUser();
        publishedPost(postId);
        FeedPostLike like = postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .orElse(null);
        if (like == null) {
            return;
        }
        postLikeRepository.delete(like);
        postRepository.decrementLikeCount(postId);
    }

    @Transactional(readOnly = true)
    public FeedPostLikeStatusDTO likeStatus(String postId) {
        User user = currentUserOrNull();
        FeedPost post = publishedPost(postId);
        return likeStatus(postId, user, post.getLikeCount());
    }

    @Transactional
    public void bookmarkPost(String postId) {
        User user = currentUser();
        FeedPost post = publishedPost(postId);
        if (bookmarkRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return;
        }
        try {
            bookmarkRepository.saveAndFlush(FeedPostBookmark.builder().post(post).user(user).build());
        } catch (DataIntegrityViolationException exception) {
            return;
        }
        postRepository.incrementBookmarkCount(postId);
    }

    @Transactional
    public void removeBookmark(String postId) {
        User user = currentUser();
        publishedPost(postId);
        FeedPostBookmark bookmark = bookmarkRepository.findByPostIdAndUserId(postId, user.getId())
                .orElse(null);
        if (bookmark == null) {
            return;
        }
        bookmarkRepository.delete(bookmark);
        postRepository.decrementBookmarkCount(postId);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedPostResponseDTO> saved(Pageable pageable) {
        User user = currentUser();
        Page<FeedPost> page = bookmarkRepository.findSavedPosts(user.getId(), FeedPostStatus.PUBLISHED, pageable);
        return PaginatedResponse.from(page.map(post -> FeedPostResponseDTO.from(post).toBuilder()
                .bookmarkedByCurrentUser(true).build()));
    }

    @Transactional
    public FeedShareResponseDTO share(String postId) {
        FeedPost post = publishedPost(postId);
        postRepository.incrementShareCount(postId);
        return new FeedShareResponseDTO("/feed/" + postId, post.getTitle(), post.getBody());
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedPostCommentResponseDTO> comments(String postId, Pageable pageable) {
        publishedPost(postId);
        User user = currentUserOrNull();
        return PaginatedResponse.from(commentRepository.findByPostIdAndParentIsNullAndDeletedAtIsNull(postId, pageable)
                .map(comment -> toComment(comment, user)));
    }

    @Transactional
    public FeedPostCommentResponseDTO addComment(String postId, FeedPostCommentCreateDTO request) {
        User user = currentUser();
        FeedPost post = publishedPost(postId);
        FeedPostComment comment = commentRepository.save(FeedPostComment.builder()
                .post(post).user(user).content(validateContent(request.getContent())).build());
        postRepository.incrementCommentCount(postId);
        if (!post.getAuthor().getId().equals(user.getId())) {
            notificationService.createForUser(post.getAuthor(), user.getName() + " commented on your post.",
                    NotificationType.Feed.POST_COMMENTED, postId);
        }
        return toComment(comment, user);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedPostCommentResponseDTO> replies(String commentId, Pageable pageable) {
        findComment(commentId);
        User user = currentUserOrNull();
        return PaginatedResponse.from(commentRepository.findByParentIdAndDeletedAtIsNull(commentId, pageable)
                .map(comment -> toComment(comment, user)));
    }

    @Transactional
    public FeedPostCommentResponseDTO reply(String commentId, FeedPostCommentCreateDTO request) {
        User user = currentUser();
        FeedPostComment parent = findComment(commentId);
        if (parent.getParent() != null) {
            throw new BadRequestException("Nested replies beyond one level are not supported.");
        }
        FeedPost post = publishedPost(parent.getPost().getId());
        FeedPostComment reply = commentRepository.save(FeedPostComment.builder()
                .post(post).user(user).parent(parent).content(validateContent(request.getContent())).build());
        commentRepository.incrementReplyCount(parent.getId());
        postRepository.incrementCommentCount(post.getId());
        if (!parent.getUser().getId().equals(user.getId())) {
            notificationService.createForUser(parent.getUser(), user.getName() + " replied to your comment.",
                    NotificationType.Feed.COMMENT_REPLIED, parent.getId());
        }
        return toComment(reply, user);
    }

    @Transactional
    public void deleteComment(String commentId) {
        User user = currentUser();
        FeedPostComment comment = findComment(commentId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean moderator = accessControlService.canModerateFeed(authentication);
        if (!moderator && !comment.getUser().getId().equals(user.getId())
                && !comment.getPost().getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("You may not delete this comment.");
        }
        comment.setDeletedAt(java.time.LocalDateTime.now(clock));
        commentRepository.save(comment);
        postRepository.decrementCommentCount(comment.getPost().getId());
        if (comment.getParent() != null) {
            commentRepository.decrementReplyCount(comment.getParent().getId());
        }
    }

    @Transactional
    public void likeComment(String commentId) {
        User user = currentUser();
        FeedPostComment comment = findComment(commentId);
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            return;
        }
        try {
            commentLikeRepository.saveAndFlush(FeedPostCommentLike.builder().comment(comment).user(user).build());
        } catch (DataIntegrityViolationException exception) {
            return;
        }
        commentRepository.incrementLikeCount(commentId);
        int likeCount = Math.toIntExact(commentLikeRepository.countByCommentId(commentId));
        if (!comment.getUser().getId().equals(user.getId())) {
            notificationService.createOrUpdateAggregatedNotification(comment.getUser(), NotificationType.Feed.COMMENT_LIKED,
                    commentId, "liked your comment", likeCount, user.getName());
        }
    }

    @Transactional
    public void unlikeComment(String commentId) {
        User user = currentUser();
        findComment(commentId);
        FeedPostCommentLike like = commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId())
                .orElse(null);
        if (like == null) {
            return;
        }
        commentLikeRepository.delete(like);
        commentRepository.decrementLikeCount(commentId);
    }

    @Transactional(readOnly = true)
    public FeedPostLikeStatusDTO commentLikeStatus(String commentId) {
        User user = currentUserOrNull();
        FeedPostComment comment = findComment(commentId);
        boolean liked = user != null && commentLikeRepository.existsByCommentIdAndUserId(commentId, user.getId());
        return new FeedPostLikeStatusDTO(comment.getLikeCount(), liked);
    }

    private FeedPostLikeStatusDTO likeStatus(String postId, User user, int count) {
        boolean liked = user != null && postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
        return new FeedPostLikeStatusDTO(count, liked);
    }

    private FeedPostCommentResponseDTO toComment(FeedPostComment comment, User user) {
        return FeedPostCommentResponseDTO.from(comment,
                user != null && commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), user.getId()));
    }

    private FeedPost publishedPost(String id) {
        FeedPost post = postRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feed post not found."));
        if (post.getStatus() != FeedPostStatus.PUBLISHED) {
            throw new ConflictException("Post is not published.");
        }
        return post;
    }

    private FeedPostComment findComment(String id) {
        return commentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
    }

    private String validateContent(String content) {
        int maxLength = appProperties.getFeed().getMaxCommentLength();
        if (content == null || content.isBlank() || (maxLength > 0 && content.trim().length() > maxLength)) {
            throw new BadRequestException("Comment exceeds the configured maximum length.");
        }
        return content.trim();
    }

    private User currentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new ForbiddenException("Authentication is required.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private User currentUserOrNull() {
        String email = SecurityUtils.getCurrentUsername();
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }
}
