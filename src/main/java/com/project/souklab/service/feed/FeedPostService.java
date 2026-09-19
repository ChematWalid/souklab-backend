package com.project.souklab.service.feed;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.analytics.AnalyticsMetadata;

import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dto.feed.FeedPostCreateDTO;
import com.project.souklab.dto.feed.FeedPostMediaResponseDTO;
import com.project.souklab.dto.feed.FeedPostModerationDTO;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.lifecycle.StorageObjectLifecycle;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostMedia;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.model.Formation;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.security.AccessControlService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Coordinates moderated public feed posts and their stored image attachments.
 */
@Service
@RequiredArgsConstructor
public class FeedPostService {

    private final FeedPostRepository postRepository;
    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final VirusScanService virusScanService;
    private final StorageService storageService;
    private final FileUrlResolver fileUrlResolver;
    private final NotificationService notificationService;
    private final StorageObjectLifecycle storageObjectLifecycle;
    private final AccessControlService accessControlService;
    private final Clock clock;
    private final AppProperties appProperties;
    private ActivityEventService activityEventService;

    @Autowired(required = false)
    void setActivityEventService(ActivityEventService value) { this.activityEventService = value; }

    /**
     * Lists public published posts.
     *
     * @param type optional post type
     * @param pageable pagination and sorting
     * @return published posts
     */
    @Transactional(readOnly = true)
    public Page<FeedPostResponseDTO> listPublic(FeedPostType type, Pageable pageable) {
        Page<FeedPost> posts = type == null
                ? postRepository.findByStatusAndDeletedAtIsNull(FeedPostStatus.PUBLISHED, pageable)
                : postRepository.findByStatusAndTypeAndDeletedAtIsNull(FeedPostStatus.PUBLISHED, type, pageable);
        return posts.map(this::toResponse);
    }

    /**
     * Finds a published post for public consumption.
     *
     * @param id post identifier
     * @return post response
     */
    @Transactional(readOnly = true)
    public FeedPostResponseDTO getPublic(String id) {
        FeedPost post = findPost(id);
        if (post.getStatus() != FeedPostStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Feed post not found.");
        }
        return toResponse(post);
    }

    /**
     * Submits a post for moderation.
     *
     * @param request post payload
     * @return pending post response
     */
    @Transactional
    public FeedPostResponseDTO create(FeedPostCreateDTO request) {
        User author = currentUser();
        validateAuthor(author);
        Formation formation = resolveFormation(request.getFormationId());
        if (request.getType() == FeedPostType.FORMATION && formation == null) {
            throw new BadRequestException("Formation posts require a formationId.");
        }
        if (request.getType() != FeedPostType.FORMATION && formation != null) {
            throw new BadRequestException("Only formation posts may reference a formation.");
        }
        FeedPost post = FeedPost.builder()
                .author(author)
                .type(request.getType())
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .formation(formation)
                .status(FeedPostStatus.PENDING)
                .build();
        return toResponse(postRepository.save(post));
    }

    /**
     * Updates a post owned by the current author or an administrator.
     *
     * @param id post identifier
     * @param request new post content
     * @return updated post
     */
    @Transactional
    public FeedPostResponseDTO update(String id, FeedPostCreateDTO request) {
        FeedPost post = findPost(id);
        requireAuthorOrAdmin(post);
        Formation formation = resolveFormation(request.getFormationId());
        if (request.getType() == FeedPostType.FORMATION && formation == null) {
            throw new BadRequestException("Formation posts require a formationId.");
        }
        if (request.getType() != FeedPostType.FORMATION && formation != null) {
            throw new BadRequestException("Only formation posts may reference a formation.");
        }
        post.setType(request.getType());
        post.setTitle(request.getTitle().trim());
        post.setBody(request.getBody().trim());
        post.setFormation(formation);
        if (!isAdmin()) {
            post.setStatus(FeedPostStatus.PENDING);
            post.setPublishedAt(null);
        }
        return toResponse(postRepository.save(post));
    }

    /**
     * Soft-removes an owned or administrator-managed post.
     *
     * @param id post identifier
     */
    @Transactional
    public void remove(String id) {
        FeedPost post = findPost(id);
        requireAuthorOrAdmin(post);
        post.setStatus(FeedPostStatus.REMOVED);
        post.setDeletedAt(LocalDateTime.now(clock));
        postRepository.save(post);
        post.getMedia().forEach(media -> storageObjectLifecycle.deleteAfterCommit(media.getStorageKey()));
    }

    /**
     * Adds an image attachment to an owned post.
     *
     * @param id post identifier
     * @param file image payload
     * @return stored media response
     */
    @Transactional
    public FeedPostMediaResponseDTO addMedia(String id, MultipartFile file) {
        FeedPost post = findPost(id);
        requireAuthorOrAdmin(post);
        int maxMediaPerPost = appProperties.getFeed().getMaxMediaPerPost();
        if (post.getMedia().size() >= maxMediaPerPost) {
            throw new BadRequestException("A feed post may contain at most " + maxMediaPerPost + " images.");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Media file is required.");
        }
        String storedKey = null;
        try {
            ValidatedFile validated = fileValidator.validateAndSanitize(
                    file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize(),
                    appProperties.getFeed().getAllowedImageMimeTypes());
            ValidatedFile scanned = virusScanService.scan(validated);
            StorageResult stored = storageService.store(scanned.content(), scanned.sanitizedFilename(), scanned.detectedMimeType(), scanned.size());
            storedKey = stored.key();
            FeedPostMedia media = FeedPostMedia.builder()
                    .post(post)
                    .storageKey(storedKey)
                    .contentType(scanned.detectedMimeType())
                    .fileSize(scanned.size())
                    .displayOrder(post.getMedia().size())
                    .build();
            post.addMedia(media);
            postRepository.save(post);
            return FeedPostMediaResponseDTO.builder()
                    .id(media.getId())
                    .url(fileUrlResolver.toUrl(media.getStorageKey()))
                    .contentType(media.getContentType())
                    .displayOrder(media.getDisplayOrder())
                    .build();
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read media upload.");
        } catch (RuntimeException exception) {
            if (storedKey != null) {
                storageService.delete(storedKey);
            }
            throw exception;
        }
    }

    /**
     * Removes an attachment from an owned post and schedules its object cleanup.
     *
     * @param id post identifier
     * @param mediaId attachment identifier
     */
    @Transactional
    public void removeMedia(String id, String mediaId) {
        FeedPost post = findPost(id);
        requireAuthorOrAdmin(post);
        FeedPostMedia media = post.getMedia().stream()
                .filter(candidate -> candidate.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Feed media not found."));
        post.removeMedia(media);
        postRepository.save(post);
        storageObjectLifecycle.deleteAfterCommit(media.getStorageKey());
    }

    /**
     * Publishes a pending post as an administrator.
     *
     * @param id post identifier
     * @param request moderation note
     * @return published post
     */
    @Transactional
    public FeedPostResponseDTO publish(String id, FeedPostModerationDTO request) {
        requireAdmin();
        FeedPost post = findPost(id);
        if (post.getStatus() == FeedPostStatus.REMOVED) {
            throw new ConflictException("Removed posts cannot be published.");
        }
        post.setModeratedBy(currentUser());
        post.setModerationNote(request.getNote().trim());
        post.setStatus(FeedPostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now(clock));
        FeedPost saved = postRepository.save(post);
        if (activityEventService != null) {
            User moderator = currentUser();
            activityEventService.record(AnalyticsEvent.Feed.Post.PUBLISHED, moderator.getId(), saved.getId(),
                    Map.of(AnalyticsMetadata.Content.POST_TYPE.value(), saved.getType().value()));
        }
        if (saved.getType() == FeedPostType.FORMATION) {
            notificationService.createForUser(saved.getAuthor(), "Your formation post was published.", NotificationType.NEW_FORMATION, saved.getId());
        }
        return toResponse(saved);
    }

    /**
     * Hides a post as an administrator.
     *
     * @param id post identifier
     * @param request moderation note
     * @return hidden post
     */
    @Transactional
    public FeedPostResponseDTO hide(String id, FeedPostModerationDTO request) {
        requireAdmin();
        FeedPost post = findPost(id);
        if (post.getStatus() == FeedPostStatus.REMOVED) {
            throw new ConflictException("Removed posts cannot be hidden.");
        }
        post.setStatus(FeedPostStatus.HIDDEN);
        post.setModeratedBy(currentUser());
        post.setModerationNote(request.getNote().trim());
        return toResponse(postRepository.save(post));
    }

    /**
     * Lists pending moderation items.
     *
     * @param pageable pagination and sorting
     * @return pending posts
     */
    @Transactional(readOnly = true)
    public Page<FeedPostResponseDTO> listPending(Pageable pageable) {
        requireAdmin();
        return postRepository.findByStatusAndDeletedAtIsNull(FeedPostStatus.PENDING, pageable).map(this::toResponse);
    }

    private FeedPost findPost(String id) {
        return postRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feed post not found."));
    }

    private Formation resolveFormation(String id) {
        return id == null || id.isBlank() ? null : formationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found."));
    }

    private FeedPostResponseDTO toResponse(FeedPost post) {
        return FeedPostResponseDTO.from(post, fileUrlResolver::toUrl);
    }

    private User currentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new ForbiddenException("Authentication is required.");
        }
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private void validateAuthor(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE
                || (!isAdmin() && (!hasArtisanContentPermission() || !isVerifiedArtisan(user)))) {
            throw new ForbiddenException("Only active verified artisans or administrators may publish feed posts.");
        }
    }

    private boolean hasArtisanContentPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accessControlService.canManageArtisanContent(authentication);
    }

    private boolean isVerifiedArtisan(User user) {
        return user.getArtisan() != null && user.getArtisan().isVerified();
    }

    private void requireAuthorOrAdmin(FeedPost post) {
        if (isAdmin()) {
            return;
        }
        User user = currentUser();
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("You may only manage your own feed posts.");
        }
    }

    private void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Administrator access is required.");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accessControlService.canModerateFeed(authentication);
    }
}
