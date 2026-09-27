package com.project.souklab.dto.feed;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record MediaOrderRequest(@NotEmpty List<@NotNull String> mediaIds) {}
