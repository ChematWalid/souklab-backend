-- Feed lifecycle, discovery metadata, engagement, and threaded comments.
ALTER TABLE feed_posts
    MODIFY status enum('DRAFT','PENDING','PUBLISHED','HIDDEN','REJECTED','REMOVED') NOT NULL,
    ADD COLUMN like_count int NOT NULL DEFAULT 0,
    ADD COLUMN comment_count int NOT NULL DEFAULT 0,
    ADD COLUMN bookmark_count int NOT NULL DEFAULT 0,
    ADD COLUMN share_count int NOT NULL DEFAULT 0;

CREATE TABLE feed_tags (
    id varchar(36) NOT NULL,
    slug varchar(80) NOT NULL,
    name varchar(80) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feed_tags_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE feed_post_tags (
    post_id varchar(36) NOT NULL,
    tag_id varchar(36) NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    KEY idx_feed_post_tags_tag (tag_id),
    CONSTRAINT fk_feed_post_tags_post FOREIGN KEY (post_id) REFERENCES feed_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_tags_tag FOREIGN KEY (tag_id) REFERENCES feed_tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE feed_post_likes (
    id varchar(36) NOT NULL,
    post_id varchar(36) NOT NULL,
    user_id varchar(36) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feed_post_likes_post_user (post_id, user_id),
    KEY idx_feed_post_likes_user (user_id),
    CONSTRAINT fk_feed_post_likes_post FOREIGN KEY (post_id) REFERENCES feed_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE feed_post_bookmarks (
    id varchar(36) NOT NULL,
    post_id varchar(36) NOT NULL,
    user_id varchar(36) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feed_post_bookmarks_post_user (post_id, user_id),
    KEY idx_feed_post_bookmarks_user_created (user_id, created_at DESC),
    CONSTRAINT fk_feed_post_bookmarks_post FOREIGN KEY (post_id) REFERENCES feed_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE feed_post_comments (
    id varchar(36) NOT NULL,
    post_id varchar(36) NOT NULL,
    user_id varchar(36) NOT NULL,
    parent_id varchar(36) DEFAULT NULL,
    content varchar(500) NOT NULL,
    like_count int NOT NULL DEFAULT 0,
    reply_count int NOT NULL DEFAULT 0,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_feed_post_comments_post (post_id, created_at DESC),
    KEY idx_feed_post_comments_parent (parent_id, created_at ASC),
    CONSTRAINT fk_feed_post_comments_post FOREIGN KEY (post_id) REFERENCES feed_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_comments_parent FOREIGN KEY (parent_id) REFERENCES feed_post_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE feed_post_comment_likes (
    id varchar(36) NOT NULL,
    comment_id varchar(36) NOT NULL,
    user_id varchar(36) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feed_post_comment_likes_comment_user (comment_id, user_id),
    KEY idx_feed_post_comment_likes_user (user_id),
    CONSTRAINT fk_feed_post_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES feed_post_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_post_comment_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

ALTER TABLE content_reports
    MODIFY target_type enum('POST','REVIEW','USER','COMMENT') NOT NULL;
