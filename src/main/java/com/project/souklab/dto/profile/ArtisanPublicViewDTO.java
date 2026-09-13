package com.project.souklab.dto.profile;

import com.project.souklab.dto.artisan.CertificationResponseDTO;
import com.project.souklab.dto.artisan.GalleryImageResponseDTO;
import com.project.souklab.dto.catalog.EpoqueSummaryDTO;
import com.project.souklab.dto.catalog.JobSubCategorySummaryDTO;
import com.project.souklab.dto.catalog.MaterialSummaryDTO;
import com.project.souklab.dto.catalog.RegionSummaryDTO;
import com.project.souklab.dto.catalog.TechniqueSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Public and gated view DTO for Artisan profiles.
 * Contains craft taxonomy, portfolio showcases, certifications, and contact masking indicators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanPublicViewDTO {

    private String id;
    private String bio;
    private String city;
    private String regionId;
    private RegionSummaryDTO region;
    private String subCategoryId;
    private JobSubCategorySummaryDTO subCategory;

    @Builder.Default
    private Set<MaterialSummaryDTO> materials = Collections.emptySet();

    @Builder.Default
    private Set<TechniqueSummaryDTO> techniques = Collections.emptySet();

    @Builder.Default
    private Set<EpoqueSummaryDTO> epoques = Collections.emptySet();

    @Builder.Default
    private List<GalleryImageResponseDTO> galleryImages = Collections.emptyList();

    @Builder.Default
    private List<CertificationResponseDTO> certifications = Collections.emptyList();

    private double rating;
    private int reviewsCount;
    private boolean teacher;
    private boolean verified;
    private String avatarUrl;
    private LocalDateTime createdAt;

    private boolean contactInfoLocked;
    private String name;
    private String phone;
    private String email;
    private String website;
    private String address;
}
