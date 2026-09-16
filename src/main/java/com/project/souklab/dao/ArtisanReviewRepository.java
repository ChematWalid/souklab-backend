package com.project.souklab.dao;

import com.project.souklab.model.ArtisanReview;
import com.project.souklab.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Persistence operations for formation-backed artisan reviews.
 */
public interface ArtisanReviewRepository extends JpaRepository<ArtisanReview, String> {
    Optional<ArtisanReview> findByIdAndDeletedAtIsNull(String id);
    Page<ArtisanReview> findByArtisanIdAndStatusAndDeletedAtIsNull(String artisanId, ReviewStatus status, Pageable pageable);
    Optional<ArtisanReview> findByEnrollmentId(String enrollmentId);

    @Query("select avg(r.rating) from ArtisanReview r where r.artisan.id = :artisanId and r.status = :status and r.deletedAt is null")
    BigDecimal averageRating(@Param("artisanId") String artisanId, @Param("status") ReviewStatus status);

    long countByArtisanIdAndStatusAndDeletedAtIsNull(String artisanId, ReviewStatus status);
}
