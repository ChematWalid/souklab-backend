package com.project.souklab.dto.chat;
import java.util.List;
public record MessagePageResponse(List<MessageResponse> content, String nextCursor, boolean last) {}
