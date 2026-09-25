package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

@Entity
@Table(name = "feed_tags", indexes = @Index(name = "uk_feed_tags_slug", columnList = "slug", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedTag extends BaseEntity {
    @KeywordField(normalizer = "artisanal_normalizer")
    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 80)
    @FullTextField(analyzer = "artisanal_name")
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeedPost> posts = new ArrayList<>();
}
