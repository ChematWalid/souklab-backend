CREATE TABLE feed_posts (
    id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    author_id VARCHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    formation_id VARCHAR(36),
    published_at DATETIME(6),
    moderated_by VARCHAR(36),
    moderation_note TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_feed_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_feed_posts_formation FOREIGN KEY (formation_id) REFERENCES formations (id),
    CONSTRAINT fk_feed_posts_moderator FOREIGN KEY (moderated_by) REFERENCES users (id)
);
CREATE INDEX idx_feed_posts_public ON feed_posts (status, deleted_at, published_at);
CREATE INDEX idx_feed_posts_author ON feed_posts (author_id, status, deleted_at);

CREATE TABLE feed_post_media (
    id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    post_id VARCHAR(36) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    display_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_feed_post_media_post FOREIGN KEY (post_id) REFERENCES feed_posts (id)
);

CREATE TABLE artisan_reviews (
    id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    reviewer_id VARCHAR(36) NOT NULL,
    artisan_id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    rating DECIMAL(3,2) NOT NULL,
    comment TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_artisan_review_enrollment UNIQUE (enrollment_id),
    CONSTRAINT fk_artisan_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES artisans (id),
    CONSTRAINT fk_artisan_reviews_artisan FOREIGN KEY (artisan_id) REFERENCES artisans (id),
    CONSTRAINT fk_artisan_reviews_enrollment FOREIGN KEY (enrollment_id) REFERENCES formation_enrollments (id)
);
CREATE INDEX idx_artisan_reviews_artisan ON artisan_reviews (artisan_id, status, created_at);
CREATE INDEX idx_artisan_reviews_reviewer ON artisan_reviews (reviewer_id, created_at);

CREATE TABLE content_reports (
    id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    reporter_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    details TEXT,
    status VARCHAR(30) NOT NULL,
    resolution_action VARCHAR(30),
    resolver_id VARCHAR(36),
    resolution_note TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_content_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_content_reports_resolver FOREIGN KEY (resolver_id) REFERENCES users (id)
);
CREATE INDEX idx_content_reports_queue ON content_reports (status, created_at);
CREATE INDEX idx_content_reports_target ON content_reports (target_type, target_id);
