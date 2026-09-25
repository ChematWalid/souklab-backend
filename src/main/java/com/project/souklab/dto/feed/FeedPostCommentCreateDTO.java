package com.project.souklab.dto.feed;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostCommentCreateDTO {
    @NotBlank
    private String content;
}
