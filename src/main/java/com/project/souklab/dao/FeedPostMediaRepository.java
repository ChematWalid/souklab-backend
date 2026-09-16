package com.project.souklab.dao;

import com.project.souklab.model.FeedPostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for feed post media attachments.
 */
public interface FeedPostMediaRepository extends JpaRepository<FeedPostMedia, String> {

    /**
     * Lists attachments ordered for presentation.
     *
     * @param postId post identifier
     * @return ordered media attachments
     */
    List<FeedPostMedia> findByPostIdOrderByDisplayOrderAsc(String postId);
}
