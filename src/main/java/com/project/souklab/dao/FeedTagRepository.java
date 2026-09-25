package com.project.souklab.dao;

import com.project.souklab.model.FeedTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FeedTagRepository extends JpaRepository<FeedTag, String> {
    List<FeedTag> findBySlugIn(Collection<String> slugs);
}
