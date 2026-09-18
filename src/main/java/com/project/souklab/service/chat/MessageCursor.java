package com.project.souklab.service.chat;

import java.time.LocalDateTime;

/** Cursor position for stable keyset pagination of conversation messages. */
record MessageCursor(LocalDateTime time, String id) {
}
