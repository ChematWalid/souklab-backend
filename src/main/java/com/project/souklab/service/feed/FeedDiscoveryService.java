package com.project.souklab.service.feed;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.FeedPostBookmarkRepository;
import com.project.souklab.dao.FeedPostLikeRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.engine.search.query.SearchResult;
import java.util.List;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
public class FeedDiscoveryService {
    private final FeedPostRepository postRepository;
    private final UserRepository userRepository;
    private final FeedPostLikeRepository postLikeRepository;
    private final FeedPostBookmarkRepository bookmarkRepository;
    private final AppProperties appProperties;
    private final EntityManager entityManager;
    private final FeedPrivacyService feedPrivacyService;

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedPostResponseDTO> list(FeedPostType type, String authorId, String tag,
                                                        String query, String sort, Pageable pageable) {
        Pageable effectivePageable = applySort(pageable, sort);
        Page<FeedPost> page = searchEnabled(query, type, authorId, tag)
                ? search(query, effectivePageable)
                : postRepository.findForDiscovery(FeedPostStatus.PUBLISHED, type, authorId,
                normalize(tag), normalize(query), effectivePageable);
        return PaginatedResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedPostResponseDTO> following(Pageable pageable) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new ForbiddenException("Authentication is required.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Authentication is required."));
        if (user.getClient() == null) {
            return PaginatedResponse.<FeedPostResponseDTO>builder()
                    .content(List.of()).pageNumber(pageable.getPageNumber()).pageSize(pageable.getPageSize())
                    .totalElements(0).totalPages(0).last(true).build();
        }
        Page<FeedPost> page = postRepository.findFollowing(user.getClient().getId(), FeedPostStatus.PUBLISHED, pageable);
        return PaginatedResponse.from(page.map(this::toResponse));
    }

    private FeedPostResponseDTO toResponse(FeedPost post) {
        FeedPostResponseDTO response = feedPrivacyService.protectPost(post, FeedPostResponseDTO.from(post));
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            return response;
        }
        return userRepository.findByEmail(email)
                .map(user -> response.toBuilder()
                        .likedByCurrentUser(postLikeRepository.existsByPostIdAndUserId(post.getId(), user.getId()))
                        .bookmarkedByCurrentUser(bookmarkRepository.existsByPostIdAndUserId(post.getId(), user.getId()))
                        .build())
                .orElse(response);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean searchEnabled(String query, FeedPostType type, String authorId, String tag) {
        return query != null && !query.isBlank() && type == null && authorId == null && tag == null
                && appProperties.getSearch().isEnabled();
    }

    private Page<FeedPost> search(String query, Pageable pageable) {
        SearchSession session = Search.session(entityManager);
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        SearchResult<FeedPost> result = session.search(FeedPost.class)
                .where(f -> f.bool(b -> b
                        .should(f.match().field("title").matching(query))
                        .should(f.match().field("body").matching(query))
                        .should(f.match().field("tags.name").matching(query))
                        .filter(f.match().field("status").matching(FeedPostStatus.PUBLISHED.value()))
                        .filter(f.not(f.exists().field("deletedAt")))))
                .sort(f -> pageable.getSort().getOrderFor("likeCount") != null
                        ? f.field("likeCount").desc()
                        : f.field("publishedAt").desc())
                .fetch(offset, pageable.getPageSize());
        List<FeedPost> content = result.hits();
        return new PageImpl<>(content, pageable, result.total().hitCount());
    }

    private Pageable applySort(Pageable pageable, String sort) {
        if (sort == null || sort.isBlank()) {
            return pageable;
        }
        Sort requested = "popular".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
                : Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), requested);
    }
}
