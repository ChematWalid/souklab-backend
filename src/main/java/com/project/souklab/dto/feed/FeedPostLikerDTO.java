package com.project.souklab.dto.feed;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;
@Value @Builder
public class FeedPostLikerDTO { String userId; String name; String avatarUrl; LocalDateTime likedAt; }
