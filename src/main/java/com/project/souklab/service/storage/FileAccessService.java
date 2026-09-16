package com.project.souklab.service.storage;

import com.project.souklab.dao.ArtisanCertificationRepository;
import com.project.souklab.dao.ArtisanGalleryImageRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dao.FormationFileRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.UserAvatarRepository;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.FormationFile;
import com.project.souklab.security.AccessControlService;
import com.project.souklab.security.Permission;
import com.project.souklab.util.ArtisanSecurityUtils;
import com.project.souklab.util.SecurityUtils;
import com.project.souklab.dao.ArtisanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies Souklab ownership and enrollment rules before an opaque stored object is served.
 * Storage providers remain unaware of application users and authorization semantics.
 */
@Service
@RequiredArgsConstructor
public class FileAccessService {

    private final UserAvatarRepository userAvatarRepository;
    private final ArtisanGalleryImageRepository galleryImageRepository;
    private final ArtisanCertificationRepository certificationRepository;
    private final FormationFileRepository formationFileRepository;
    private final FormationRepository formationRepository;
    private final FormationEnrollmentRepository formationEnrollmentRepository;
    private final ArtisanRepository artisanRepository;
    private final FileUrlResolver fileUrlResolver;
    private final AccessControlService accessControlService;

    /**
     * Verifies that the current principal may retrieve the stored object identified by the key.
     *
     * @param storageKey opaque storage key supplied by the route
     * @return whether the response represents intentionally public media and may be browser-cached
     */
    @Transactional(readOnly = true)
    public boolean authorize(String storageKey) {
        if (userAvatarRepository.findFirstByStorageKeyOriginalOrStorageKeyMediumOrStorageKeyThumbnail(
                storageKey, storageKey, storageKey).isPresent()) {
            return true;
        }

        String objectUrl = fileUrlResolver.toUrl(storageKey);
        if (galleryImageRepository.findByImageUrlAndDeletedAtIsNull(objectUrl).isPresent()) {
            return true;
        }

        if (formationRepository.findByThumbnailUrlAndDeletedAtIsNull(objectUrl).isPresent()) {
            return true;
        }

        if (certificationRepository.findByDocumentUrlAndDeletedAtIsNull(objectUrl).isPresent()) {
            requireCertificationOwnerOrAdministrator(storageKey);
            return false;
        }

        FormationFile file = formationFileRepository.findByStorageKeyAndDeletedAtIsNull(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("File not found."));
        requireFormationAccess(file);
        return false;
    }

    private void requireCertificationOwnerOrAdministrator(String storageKey) {
        String objectUrl = fileUrlResolver.toUrl(storageKey);
        var certification = certificationRepository.findByDocumentUrlAndDeletedAtIsNull(objectUrl)
                .orElseThrow(() -> new ResourceNotFoundException("File not found."));
        if (isAdministrator() || certification.getArtisan().getUser().getEmail().equalsIgnoreCase(SecurityUtils.getCurrentUsername())) {
            return;
        }
        throw new ForbiddenException("Access denied.");
    }

    private void requireFormationAccess(FormationFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (accessControlService.hasPermission(authentication, Permission.ADMIN_FORMATIONS)) {
            return;
        }
        var artisan = ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository);
        var formation = file.getFormation();
        boolean isAuthor = formation.getAuthor().getId().equals(artisan.getId());
        boolean isEnrolled = formationEnrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(), artisan.getId(), EnrollmentStatus.CONFIRMED);
        if (!isAuthor && !isEnrolled) {
            throw new ForbiddenException("Access denied.");
        }
    }

    private boolean isAdministrator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accessControlService.hasPermission(authentication, Permission.ADMIN_USERS);
    }
}
