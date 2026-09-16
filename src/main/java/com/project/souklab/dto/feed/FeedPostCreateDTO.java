package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for submitting a feed post for moderation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostCreateDTO {

    @NotNull
    private FeedPostType type;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String body;

    @Size(max = 36)
    private String formationId;
}
