package com.project.souklab.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Administrator note supplied during a feed moderation action.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostModerationDTO {
    @NotBlank
    @Size(max = 2000)
    private String note;
}
