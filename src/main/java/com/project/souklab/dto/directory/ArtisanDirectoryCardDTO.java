package com.project.souklab.dto.directory;

import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanGalleryImage;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import com.project.souklab.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight public directory card DTO representing an artisan in search results,
 * regional directories, and taxonomy discovery grids.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanDirectoryCardDTO {

    private static final int DEFAULT_BIO_SNIPPET_LENGTH = 160;
    private static final int MAX_PRIMARY_BADGES = 4;

    private String id;
    private String artisanName;
    private String avatarUrl;
    private String coverImageUrl;
    private String bioSnippet;
    private String city;
    private String wilayaName;
    private String wilayaCode;
    private String regionSlug;
    private String categoryName;
    private String categorySlug;
    private String subCategoryName;
    private String subCategorySlug;
    private double rating;
    private int reviewsCount;
    private int viewsCount;
    private boolean verified;
    private boolean premium;
    private boolean teacher;

    @Builder.Default
    private List<String> primaryMaterials = new ArrayList<>();

    @Builder.Default
    private List<String> primaryTechniques = new ArrayList<>();

    private LocalDateTime createdAt;

    /**
     * Maps an {@link Artisan} domain entity into a lightweight public directory card DTO.
     *
     * @param artisan the artisan entity
     * @return populated directory card DTO, or null if artisan is null
     */
    public static ArtisanDirectoryCardDTO from(Artisan artisan) {
        if (artisan == null) {
            return null;
        }

        User user = artisan.getUser();
        Region region = artisan.getRegion();
        JobSubCategory subCat = artisan.getSubCategory();
        JobCategory cat = subCat != null ? subCat.getCategory() : null;

        return ArtisanDirectoryCardDTO.builder()
                .id(artisan.getId())
                .artisanName(user != null ? user.getName() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .coverImageUrl(extractCoverImageUrl(artisan.getGalleryImages()))
                .bioSnippet(truncateBio(artisan.getBio(), DEFAULT_BIO_SNIPPET_LENGTH))
                .city(artisan.getCity())
                .wilayaName(resolveWilayaName(region))
                .wilayaCode(resolveWilayaCode(region))
                .regionSlug(region != null ? region.getSlug() : null)
                .categoryName(cat != null ? cat.getName() : null)
                .categorySlug(cat != null ? cat.getSlug() : null)
                .subCategoryName(subCat != null ? subCat.getName() : null)
                .subCategorySlug(subCat != null ? subCat.getSlug() : null)
                .rating(artisan.getRating())
                .reviewsCount(artisan.getReviewsCount())
                .viewsCount(artisan.getViewsCount())
                .verified(artisan.isVerified())
                .premium(artisan.isPremium())
                .teacher(artisan.isTeacher())
                .primaryMaterials(extractPrimaryMaterials(artisan))
                .primaryTechniques(extractPrimaryTechniques(artisan))
                .createdAt(artisan.getCreatedAt())
                .build();
    }

    /**
     * Truncates biographical text cleanly on a word boundary.
     *
     * @param bio raw biography narrative
     * @param maxLength maximum allowed characters before ellipsis
     * @return truncated bio snippet with ellipsis, or original string if shorter
     */
    public static String truncateBio(String bio, int maxLength) {
        if (bio == null || bio.isBlank()) {
            return "";
        }
        String trimmed = bio.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        int lastSpace = trimmed.lastIndexOf(' ', maxLength);
        if (lastSpace > maxLength / 2) {
            return trimmed.substring(0, lastSpace) + "...";
        }
        return trimmed.substring(0, maxLength) + "...";
    }

    /**
     * Extracts the primary cover image URL from the artisan gallery list.
     */
    private static String extractCoverImageUrl(List<ArtisanGalleryImage> galleryImages) {
        if (galleryImages == null || galleryImages.isEmpty()) {
            return null;
        }
        return galleryImages.stream()
                .filter(img -> img.getImageUrl() != null && !img.getImageUrl().isBlank())
                .min((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                .map(ArtisanGalleryImage::getImageUrl)
                .orElse(null);
    }

    /**
     * Resolves the primary administrative Wilaya name from a region hierarchy.
     */
    private static String resolveWilayaName(Region region) {
        if (region == null) {
            return null;
        }
        if (region.getParent() != null && !isRootCountryCode(region.getParent().getCode())) {
            return region.getParent().getName();
        }
        return region.getName();
    }

    /**
     * Resolves the administrative Wilaya code from a region hierarchy.
     */
    private static String resolveWilayaCode(Region region) {
        if (region == null) {
            return null;
        }
        if (region.getParent() != null && !isRootCountryCode(region.getParent().getCode())) {
            return region.getParent().getCode();
        }
        return region.getCode();
    }

    private static boolean isRootCountryCode(String code) {
        return "DZ".equalsIgnoreCase(code) || "FR".equalsIgnoreCase(code);
    }

    private static List<String> extractPrimaryMaterials(Artisan artisan) {
        if (artisan.getMaterials() == null || artisan.getMaterials().isEmpty()) {
            return Collections.emptyList();
        }
        return artisan.getMaterials().stream()
                .map(Material::getName)
                .limit(MAX_PRIMARY_BADGES)
                .toList();
    }

    private static List<String> extractPrimaryTechniques(Artisan artisan) {
        if (artisan.getTechniques() == null || artisan.getTechniques().isEmpty()) {
            return Collections.emptyList();
        }
        return artisan.getTechniques().stream()
                .map(Technique::getName)
                .limit(MAX_PRIMARY_BADGES)
                .toList();
    }
}
