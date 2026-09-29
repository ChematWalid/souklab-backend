package com.project.souklab.dao.specification;

import com.project.souklab.model.Artisan;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Region;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JPA specifications for filtering public formation catalog queries.
 * Supports filtering by online status, author craft trade/category, and author region.
 */
public final class FormationSpecifications {

    private FormationSpecifications() {
    }

    /**
     * Composes specification filters for published, non-deleted formations with optional
     * trade, region, and online/in-person delivery criteria.
     *
     * @param trade trade/category identifier or slug matching author's trade specialization
     * @param region region identifier, code, or slug matching author's geographic area
     * @param online delivery mode filter (true for online only, false for in-person only, null for all)
     * @return composite specification
     */
    public static Specification<Formation> filterCatalog(String trade, String region, Boolean online) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only published, non-soft-deleted formations are catalog-eligible
            predicates.add(cb.equal(root.get("status"), FormationStatus.PUBLISHED));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (online != null) {
                predicates.add(cb.equal(root.get("isOnline"), online));
            }

            Join<Formation, Artisan> author = null;

            if (trade != null && !trade.isBlank()) {
                author = root.join("author", JoinType.INNER);
                Join<Artisan, JobSubCategory> subCategory = author.join("subCategory", JoinType.INNER);
                Join<JobSubCategory, JobCategory> category = subCategory.join("category", JoinType.LEFT);
                String trimmedTrade = trade.trim();
                predicates.add(cb.or(
                        cb.equal(subCategory.get("id"), trimmedTrade),
                        cb.equal(subCategory.get("slug"), trimmedTrade),
                        cb.equal(category.get("id"), trimmedTrade),
                        cb.equal(category.get("slug"), trimmedTrade)
                ));
            }

            if (region != null && !region.isBlank()) {
                if (author == null) {
                    author = root.join("author", JoinType.INNER);
                }
                Join<Artisan, Region> regionJoin = author.join("region", JoinType.INNER);
                Join<Region, Region> parentRegion = regionJoin.join("parent", JoinType.LEFT);
                String trimmedRegion = region.trim();
                predicates.add(cb.or(
                        cb.equal(regionJoin.get("id"), trimmedRegion),
                        cb.equal(regionJoin.get("slug"), trimmedRegion),
                        cb.equal(regionJoin.get("code"), trimmedRegion),
                        cb.equal(parentRegion.get("id"), trimmedRegion),
                        cb.equal(parentRegion.get("slug"), trimmedRegion),
                        cb.equal(parentRegion.get("code"), trimmedRegion)
                ));
            }

            if (author != null && query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
