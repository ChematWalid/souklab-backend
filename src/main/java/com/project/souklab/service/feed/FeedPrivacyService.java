package com.project.souklab.service.feed;

import com.project.souklab.dto.feed.FeedPostCommentResponseDTO;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostComment;
import com.project.souklab.model.User;
import com.project.souklab.security.ViewerPremiumResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Applies the shared premium-aware artisan identity policy to feed responses. */
@Service
@RequiredArgsConstructor
public class FeedPrivacyService {

    private final ViewerPremiumResolver viewerPremiumResolver;

    public FeedPostResponseDTO protectPost(FeedPost post, FeedPostResponseDTO response) {
        User author = post.getAuthor();
        if (author == null || author.getArtisan() == null
                || !viewerPremiumResolver.isContactInfoLockedFor(author)) {
            return response;
        }
        return response.toBuilder().authorName(maskedName(author.getArtisan().getId())).build();
    }

    public FeedPostCommentResponseDTO protectComment(FeedPostComment comment,
                                                      FeedPostCommentResponseDTO response) {
        User author = comment.getUser();
        if (author == null || author.getArtisan() == null
                || !viewerPremiumResolver.isContactInfoLockedFor(author)) {
            return response;
        }
        return response.toBuilder().authorName(maskedName(author.getArtisan().getId())).build();
    }

    private String maskedName(String artisanId) {
        if (artisanId == null || artisanId.isBlank()) {
            return "Artisan #?????";
        }
        String suffix = artisanId.length() >= 5
                ? artisanId.substring(artisanId.length() - 5)
                : artisanId;
        return "Artisan #" + suffix.toUpperCase(Locale.ROOT);
    }
}
