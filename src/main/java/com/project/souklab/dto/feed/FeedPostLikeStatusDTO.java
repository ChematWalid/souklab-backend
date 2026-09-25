package com.project.souklab.dto.feed;

import lombok.Value;

@Value
public class FeedPostLikeStatusDTO {
    int likeCount;
    boolean likedByCurrentUser;
}
