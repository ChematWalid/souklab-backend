package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPostType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private String title;

    @NotBlank
    private String body;

    @Size(max = 36)
    private String formationId;

    @JsonProperty("isDraft")
    private boolean isDraft;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    public FeedPostCreateDTO(FeedPostType type, String title, String body, String formationId) {
        this(type, title, body, formationId, false, new ArrayList<>());
    }
}
