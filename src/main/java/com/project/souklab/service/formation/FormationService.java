package com.project.souklab.service.formation;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.analytics.AnalyticsMetadata;

import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dao.FormationFileRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.FormationReviewRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formation.FormationCreateDTO;
import com.project.souklab.dto.formation.FormationFileResponseDTO;
import com.project.souklab.dto.formation.FormationResponseDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.dto.formation.FormationUpdateDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.FileUrlResolver;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.lifecycle.StorageObjectLifecycle;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationFile;
import com.project.souklab.model.FormationReview;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.util.ArtisanSecurityUtils;
import com.project.souklab.security.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Service managing artisan formation and masterclass authoring, media attachments,
 * syllabus document management, submission workflows, and lifecycle operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormationService {

    private final FormationRepository formationRepository;
    private final ArtisanRepository artisanRepository;
    private final FormationFileRepository formationFileRepository;
    private final FormationEnrollmentRepository formationEnrollmentRepository;
    private final FormationReviewRepository formationReviewRepository;
    private final StorageService storageService;
    private final StorageObjectLifecycle storageObjectLifecycle;
    private final FileUrlResolver fileUrlResolver;
    private final FileValidator fileValidator;
    private final VirusScanService virusScanService;
    private final AppProperties appProperties;
    private final NotificationService notificationService;
    private final Clock clock;
    private ActivityEventService activityEventService;

    @Autowired(required = false)
    void setActivityEventService(ActivityEventService value) { this.activityEventService = value; }


    /**
     * Creates a new formation masterclass in draft state for the authenticated accredited instructor artisan.
     *
     * @param dto input creation payload
     * @return response DTO representing the persisted draft formation
     */
    @Transactional
    public FormationResponseDTO createFormation(FormationCreateDTO dto) {
        Artisan artisan = resolveAuthenticatedArtisan();
        if (!artisan.isTeacher()) {
            throw new ForbiddenException("Only accredited master artisans can create formations.");
        }

        Formation formation = Formation.builder()
                .author(artisan)
                .title(dto.getTitle().trim())
                .description(dto.getDescription().trim())
                .location(dto.getLocation() != null ? dto.getLocation().trim() : null)
                .isOnline(dto.isOnline())
                .scheduledAt(dto.getScheduledAt())
                .durationHours(dto.getDurationHours())
                .maxParticipants(dto.getMaxParticipants())
                .price(dto.getPrice())
                .currency(dto.getCurrency() != null && !dto.getCurrency().isBlank()
                        ? dto.getCurrency().trim()
                        : appProperties.getFormation().getDefaultCurrency())
                .status(FormationStatus.DRAFT)
                .build();

        Formation saved = formationRepository.save(formation);
        log.info("Artisan '{}' created formation draft '{}' [{}]", artisan.getId(), saved.getTitle(), saved.getId());
        return mapToResponseDTO(saved);
    }

    /**
     * Updates an authored formation. If the formation was approved or published and core metadata changed,
     * resets status to PENDING_REVIEW.
     *
     * @param id formation unique identifier
     * @param dto update payload
     * @return updated formation response DTO
     */
    @Transactional
    public FormationResponseDTO updateFormation(String id, FormationUpdateDTO dto) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);

        boolean coreMetadataChanged = !Objects.equals(dto.getScheduledAt(), formation.getScheduledAt())
                || dto.getPrice() != formation.getPrice()
                || dto.getMaxParticipants() != formation.getMaxParticipants();

        if ((formation.getStatus() == FormationStatus.APPROVED || formation.getStatus() == FormationStatus.PUBLISHED)
                && coreMetadataChanged) {
            formation.setStatus(FormationStatus.PENDING_REVIEW);
            log.info("Formation '{}' status reset to PENDING_REVIEW due to core metadata changes", formation.getId());
        }

        formation.setTitle(dto.getTitle().trim());
        formation.setDescription(dto.getDescription().trim());
        formation.setLocation(dto.getLocation() != null ? dto.getLocation().trim() : null);
        formation.setOnline(dto.isOnline());
        formation.setScheduledAt(dto.getScheduledAt());
        formation.setDurationHours(dto.getDurationHours());
        formation.setMaxParticipants(dto.getMaxParticipants());
        formation.setPrice(dto.getPrice());
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            formation.setCurrency(dto.getCurrency().trim());
        }

        Formation saved = formationRepository.save(formation);
        return mapToResponseDTO(saved);
    }

    /**
     * Uploads and stores a showcase thumbnail photograph for the formation with compensating rollback.
     *
     * @param id formation unique identifier
     * @param file multipart image file
     * @return updated formation response DTO
     */
    @Transactional
    public FormationResponseDTO uploadThumbnail(String id, MultipartFile file) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Thumbnail file is required.");
        }

        long maxBytes = appProperties.getFormation().getThumbnail().getMaxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(file.getSize(), maxBytes);
        }

        ValidatedFile validatedFile = validateAndSanitize(file, appProperties.getFormation().getThumbnail().getAllowedMimeTypes());
        ValidatedFile scannedFile = virusScanService.scan(validatedFile);

        String storageKey = null;
        try {
            StorageResult result = storageService.store(
                    scannedFile.content(),
                    scannedFile.sanitizedFilename(),
                    scannedFile.detectedMimeType(),
                    scannedFile.size()
            );
            storageKey = result.key();
            formation.setThumbnailUrl(appProperties.getStorage().toUrl(storageKey));
            Formation saved = formationRepository.save(formation);
            return mapToResponseDTO(saved);
        } catch (Exception ex) {
            compensateStorageDelete(storageKey);
            throw ex;
        }
    }

    /**
     * Uploads a course syllabus or resource document attachment with compensating rollback on database error.
     *
     * @param id formation unique identifier
     * @param file multipart attachment file
     * @return response DTO for the stored file
     */
    @Transactional
    public FormationFileResponseDTO uploadCourseFile(String id, MultipartFile file) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Course file is required.");
        }

        int maxCount = appProperties.getFormation().getFile().getMaxCount();
        long currentCount = formationFileRepository.findByFormationIdAndDeletedAtIsNull(formation.getId()).size();
        if (currentCount >= maxCount) {
            throw new BadRequestException("Course file quota reached. Maximum " + maxCount + " files allowed.");
        }

        long maxBytes = appProperties.getFormation().getFile().getMaxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(file.getSize(), maxBytes);
        }

        ValidatedFile validatedFile = validateAndSanitize(file, appProperties.getFormation().getFile().getAllowedMimeTypes());
        ValidatedFile scannedFile = virusScanService.scan(validatedFile);

        String storageKey = null;
        try {
            StorageResult result = storageService.store(
                    scannedFile.content(),
                    scannedFile.sanitizedFilename(),
                    scannedFile.detectedMimeType(),
                    scannedFile.size()
            );
            storageKey = result.key();

            FormationFile courseFile = FormationFile.builder()
                    .formation(formation)
                    .storageKey(storageKey)
                    .originalFilename(scannedFile.sanitizedFilename())
                    .contentType(scannedFile.detectedMimeType())
                    .fileSize(scannedFile.size())
                    .build();

            FormationFile savedFile = formationFileRepository.save(courseFile);
            return FormationFileResponseDTO.from(savedFile, appProperties.getStorage().resolveFileServingPrefix());
        } catch (Exception ex) {
            compensateStorageDelete(storageKey);
            throw ex;
        }
    }

    /**
     * Soft deletes an attachment file associated with an authored formation.
     *
     * @param formationId formation unique identifier
     * @param fileId course file unique identifier
     */
    @Transactional
    public void deleteCourseFile(String formationId, String fileId) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(formationId, artisan);

        FormationFile file = formationFileRepository.findByIdAndFormationIdAndDeletedAtIsNull(fileId, formation.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course file not found with id: " + fileId));

        file.setDeletedAt(LocalDateTime.now(clock));
        formationFileRepository.save(file);
        storageObjectLifecycle.deleteAfterCommit(file.getStorageKey());
    }

    /**
     * Submits a draft or rejected formation for administrative review and notifies system administrators.
     *
     * @param id formation unique identifier
     * @return updated formation response DTO
     */
    @Transactional
    public FormationResponseDTO submitForReview(String id) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);

        if (formation.getStatus() != FormationStatus.DRAFT && formation.getStatus() != FormationStatus.REJECTED) {
            throw new ConflictException("Formation can only be submitted for review from DRAFT or REJECTED status.");
        }

        if (formation.getTitle() == null || formation.getTitle().isBlank()
                || formation.getDescription() == null || formation.getDescription().isBlank()
                || formation.getScheduledAt() == null
                || formation.getDurationHours() <= 0
                || formation.getMaxParticipants() <= 0) {
            throw new BadRequestException("Required fields missing or invalid for review submission.");
        }

        formation.setStatus(FormationStatus.PENDING_REVIEW);
        Formation saved = formationRepository.save(formation);

        if (activityEventService != null) {
            activityEventService.record(AnalyticsEvent.Formation.SUBMITTED, artisan.getId(), saved.getId(),
                Map.of(AnalyticsMetadata.State.STATUS, saved.getStatus()));
        }

        notificationService.notifyAdmins("New formation submitted for review: " + saved.getTitle());
        log.info("Formation '{}' submitted for administrative review by artisan '{}'", saved.getId(), artisan.getId());

        return mapToResponseDTO(saved);
    }

    /**
     * Soft deletes an authored formation.
     *
     * @param id formation unique identifier
     */
    @Transactional
    public void deleteFormation(String id) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);
        LocalDateTime deletedAt = LocalDateTime.now(clock);

        for (FormationFile file : formationFileRepository.findByFormationIdAndDeletedAtIsNull(formation.getId())) {
            file.setDeletedAt(deletedAt);
            formationFileRepository.save(file);
            storageObjectLifecycle.deleteAfterCommit(file.getStorageKey());
        }

        formation.setDeletedAt(deletedAt);
        formationRepository.save(formation);
        storageObjectLifecycle.deleteAfterCommit(fileUrlResolver.toStorageKey(formation.getThumbnailUrl()));
        log.info("Formation '{}' soft deleted by author '{}'", formation.getId(), artisan.getId());
    }

    /**
     * Retrieves all active formations authored by the authenticated artisan.
     *
     * @param pageable pagination parameters
     * @return paginated response of formation summary cards
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<FormationSummaryDTO> getMyFormations(Pageable pageable) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Page<Formation> page = formationRepository.findByAuthorIdAndDeletedAtIsNull(artisan.getId(), pageable);
        return PaginatedResponse.from(page.map(this::mapToSummaryDTO));
    }

    /**
     * Retrieves published masterclasses authored by a specific instructor artisan.
     *
     * @param teacherArtisanId unique identifier of the teacher artisan
     * @param pageable pagination parameters
     * @return paginated response of published formations
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<FormationSummaryDTO> getTeacherFormations(String teacherArtisanId, Pageable pageable) {
        Page<Formation> page = formationRepository.findByAuthorIdAndStatusAndDeletedAtIsNull(
                teacherArtisanId, FormationStatus.PUBLISHED, pageable);
        return PaginatedResponse.from(page.map(this::mapToSummaryDTO));
    }

    /**
     * Retrieves full formation details for an authored formation.
     *
     * @param id formation unique identifier
     * @return complete formation response DTO
     */
    @Transactional(readOnly = true)
    public FormationResponseDTO getFormationDetails(String id) {
        Artisan artisan = resolveAuthenticatedArtisan();
        Formation formation = findFormationAndVerifyOwnership(id, artisan);
        return mapToResponseDTO(formation);
    }

    /**
     * Validates and sanitizes a multipart file payload against permitted MIME types.
     *
     * @param file multipart upload payload
     * @param allowedMimes permitted MIME types
     * @return validated file descriptor
     */
    private ValidatedFile validateAndSanitize(MultipartFile file, List<String> allowedMimes) {
        try {
            return fileValidator.validateAndSanitize(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    allowedMimes
            );
        } catch (IOException e) {
            log.error("Failed to read upload stream for file '{}'", file.getOriginalFilename(), e);
            throw new StorageException("Failed to read upload stream: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes stored file from physical storage backend to compensate for persistence failures.
     *
     * @param storageKey physical storage identifier
     */
    private void compensateStorageDelete(String storageKey) {
        if (storageKey != null) {
            try {
                storageService.delete(storageKey);
            } catch (Exception deleteEx) {
                log.error("Compensating storage delete failed for key '{}'", storageKey, deleteEx);
            }
        }
    }

    /**
     * Finds active formation and asserts author ownership.
     *
     * @param id formation unique identifier
     * @param artisan authenticated artisan
     * @return verified formation entity
     */
    private Formation findFormationAndVerifyOwnership(String id, Artisan artisan) {
        Formation formation = formationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with id: " + id));

        if (!formation.getAuthor().getId().equals(artisan.getId())) {
            throw new ForbiddenException("You do not have permission to modify this formation.");
        }
        return formation;
    }

    /**
     * Delegates artisan identity resolution to the shared {@link ArtisanSecurityUtils} component.
     *
     * @return resolved Artisan entity for the current authenticated principal
     */
    private Artisan resolveAuthenticatedArtisan() {
        return ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository, Permission.Artisan.FORMATIONS);
    }

    /**
     * Maps a Formation entity to a full FormationResponseDTO.
     *
     * @param formation the formation entity
     * @return populated response DTO
     */
    private FormationResponseDTO mapToResponseDTO(Formation formation) {
        List<FormationFile> activeFiles = formationFileRepository.findByFormationIdAndDeletedAtIsNull(formation.getId());
        List<FormationReview> reviews = formationReviewRepository.findByFormationIdOrderByReviewedAtDesc(formation.getId());
        long activeEnrollments = formationEnrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.CONFIRMED);
        return FormationResponseDTO.from(formation, activeFiles, reviews, activeEnrollments, appProperties.getStorage().resolveFileServingPrefix());
    }

    /**
     * Maps a Formation entity to a lightweight FormationSummaryDTO.
     *
     * @param formation the formation entity
     * @return populated summary DTO
     */
    private FormationSummaryDTO mapToSummaryDTO(Formation formation) {
        long activeEnrollments = formationEnrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.CONFIRMED);
        return FormationSummaryDTO.from(formation, activeEnrollments);
    }
}
