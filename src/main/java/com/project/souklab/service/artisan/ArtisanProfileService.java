package com.project.souklab.service.artisan;

import com.project.souklab.dao.ArtisanCertificationRepository;
import com.project.souklab.dao.ArtisanGalleryImageRepository;
import com.project.souklab.dao.ArtisanProfileViewRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.artisan.CertificationResponseDTO;
import com.project.souklab.dto.artisan.GalleryImageResponseDTO;
import com.project.souklab.dto.catalog.EpoqueSummaryDTO;
import com.project.souklab.dto.catalog.JobSubCategorySummaryDTO;
import com.project.souklab.dto.catalog.MaterialSummaryDTO;
import com.project.souklab.dto.catalog.RegionSummaryDTO;
import com.project.souklab.dto.catalog.TechniqueSummaryDTO;
import com.project.souklab.dto.profile.ArtisanPublicViewDTO;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanProfileView;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing artisan profile retrieval, contact privacy gating,
 * view counts tracking, and taxonomy and portfolio assembly.
 */
@Service
@RequiredArgsConstructor
public class ArtisanProfileService {

    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final ArtisanProfileViewRepository artisanProfileViewRepository;
    private final ArtisanGalleryImageRepository artisanGalleryImageRepository;
    private final ArtisanCertificationRepository artisanCertificationRepository;

    /**
     * Retrieves an artisan's profile for authenticated viewers.
     * Applies account status and email verification gating on the viewer (bypassed for admins),
     * records deduplicated profile views, gates sensitive contact info based on viewer premium status,
     * and compiles craft taxonomy, showcase images, and professional certifications.
     *
     * @param artisanId the ID of the target artisan to view
     * @return ArtisanPublicViewDTO containing the public/gated artisan profile
     */
    @Transactional
    public ArtisanPublicViewDTO getArtisanProfile(String artisanId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated.");
        }

        User viewer = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        boolean isAdmin = viewer.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            if (viewer.getStatus() != AccountStatus.ACTIVE) {
                throw new ForbiddenException("Your account is not active.");
            }
            if (!viewer.isEmailVerified()) {
                throw new ForbiddenException("Please verify your email address to access artisan profiles.");
            }
        }

        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan not found with id: " + artisanId));

        boolean isSelf = viewer.getId().equals(artisan.getId());

        if (!isSelf && !isAdmin) {
            boolean alreadyViewed = artisanProfileViewRepository.existsByViewerIdAndArtisanId(viewer.getId(), artisan.getId());
            if (!alreadyViewed) {
                ArtisanProfileView view = ArtisanProfileView.builder()
                        .viewer(viewer)
                        .artisan(artisan)
                        .build();
                artisanProfileViewRepository.save(view);

                artisan.setViewsCount(artisan.getViewsCount() + 1);
                artisanRepository.save(artisan);
            }
        }

        boolean contactInfoLocked;
        if (isSelf || isAdmin) {
            contactInfoLocked = false;
        } else {
            boolean isPremium = false;
            if (viewer.getClient() != null) {
                isPremium = viewer.getClient().isPremium();
            } else if (viewer.getArtisan() != null) {
                isPremium = viewer.getArtisan().isPremium();
            }
            contactInfoLocked = !isPremium;
        }

        User targetUser = artisan.getUser();
        String name;
        String phone;
        String contactEmail;
        String website;
        String address;

        if (contactInfoLocked) {
            name = "Artisan #" + (artisan.getId().length() >= 5
                    ? artisan.getId().substring(artisan.getId().length() - 5).toUpperCase()
                    : artisan.getId().toUpperCase());
            phone = null;
            contactEmail = null;
            website = null;
            address = null;
        } else {
            String resolvedName = targetUser != null ? targetUser.getName() : null;
            if (resolvedName == null || resolvedName.isBlank()) {
                if (targetUser != null && (targetUser.getFirstName() != null || targetUser.getLastName() != null)) {
                    resolvedName = ((targetUser.getFirstName() != null ? targetUser.getFirstName() : "") + " " +
                            (targetUser.getLastName() != null ? targetUser.getLastName() : "")).trim();
                }
            }
            name = resolvedName;
            phone = targetUser != null ? targetUser.getPhone() : null;
            contactEmail = targetUser != null ? targetUser.getEmail() : null;
            website = artisan.getWebsite();
            address = artisan.getAddress();
        }

        RegionSummaryDTO regionSummary = RegionSummaryDTO.from(artisan.getRegion());
        JobSubCategorySummaryDTO subCategorySummary = JobSubCategorySummaryDTO.from(artisan.getSubCategory());

        Set<MaterialSummaryDTO> materialSummaries = artisan.getMaterials() != null
                ? artisan.getMaterials().stream().map(MaterialSummaryDTO::from).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<TechniqueSummaryDTO> techniqueSummaries = artisan.getTechniques() != null
                ? artisan.getTechniques().stream().map(TechniqueSummaryDTO::from).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<EpoqueSummaryDTO> epoqueSummaries = artisan.getEpoques() != null
                ? artisan.getEpoques().stream().map(EpoqueSummaryDTO::from).collect(Collectors.toSet())
                : Collections.emptySet();

        List<GalleryImageResponseDTO> galleryImageDTOs = artisanGalleryImageRepository
                .findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc(artisan.getId())
                .stream()
                .map(GalleryImageResponseDTO::from)
                .toList();

        List<CertificationResponseDTO> certificationDTOs = artisanCertificationRepository
                .findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc(artisan.getId())
                .stream()
                .map(cert -> {
                    if (contactInfoLocked) {
                        return CertificationResponseDTO.builder()
                                .id(cert.getId())
                                .title(cert.getTitle())
                                .issuer(cert.getIssuer())
                                .issuedAt(cert.getIssuedAt())
                                .expiresAt(cert.getExpiresAt())
                                .isVerified(cert.isVerified())
                                .documentUrl(null)
                                .build();
                    }
                    return CertificationResponseDTO.from(cert);
                })
                .toList();

        return ArtisanPublicViewDTO.builder()
                .id(artisan.getId())
                .bio(artisan.getBio())
                .city(artisan.getCity())
                .regionId(artisan.getRegionId())
                .region(regionSummary)
                .subCategoryId(artisan.getSubCategoryId())
                .subCategory(subCategorySummary)
                .materials(materialSummaries)
                .techniques(techniqueSummaries)
                .epoques(epoqueSummaries)
                .galleryImages(galleryImageDTOs)
                .certifications(certificationDTOs)
                .rating(artisan.getRating())
                .reviewsCount(artisan.getReviewsCount())
                .teacher(artisan.isTeacher())
                .verified(artisan.isVerified())
                .avatarUrl(targetUser != null ? targetUser.getAvatarUrl() : null)
                .createdAt(artisan.getCreatedAt())
                .contactInfoLocked(contactInfoLocked)
                .name(name)
                .phone(phone)
                .email(contactEmail)
                .website(website)
                .address(address)
                .build();
    }
}
