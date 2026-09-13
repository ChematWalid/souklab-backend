package com.project.souklab.service.formation;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dao.FormationFileRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dto.formation.FormationEnrollmentDetailDTO;
import com.project.souklab.dto.formation.FormationEnrollmentResponseDTO;
import com.project.souklab.dto.formation.FormationPublicViewDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationEnrollment;
import com.project.souklab.model.FormationFile;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.util.ArtisanSecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing peer artisan workshop discovery, enrollment capacity enforcement,
 * cancellation policy deadlines, and protected course file access.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormationEnrollmentService {

    private final FormationRepository formationRepository;
    private final FormationEnrollmentRepository formationEnrollmentRepository;
    private final ArtisanRepository artisanRepository;
    private final FormationFileRepository formationFileRepository;
    private final StorageService storageService;
    private final AppProperties appProperties;


    /**
     * Retrieves paginated published masterclasses for peer artisan catalog discovery.
     *
     * @param pageable pagination and sorting parameters
     * @return page of formation summary cards
     */
    @Transactional(readOnly = true)
    public Page<FormationSummaryDTO> getPublishedCatalog(Pageable pageable) {
        Pageable effectivePageable = (pageable != null && pageable.getSort().isSorted())
                ? pageable
                : (pageable != null)
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "scheduledAt"))
                : PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "scheduledAt"));

        Page<Formation> formations = formationRepository.findByStatusAndDeletedAtIsNull(FormationStatus.PUBLISHED, effectivePageable);

        return formations.map(formation -> {
            long activeEnrollments = formationEnrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.CONFIRMED);
            return FormationSummaryDTO.from(formation, activeEnrollments);
        });
    }

    /**
     * Retrieves detailed public information for a published formation, including capacity,
     * caller enrollment status, and course syllabus file descriptors.
     *
     * @param formationId unique identifier of the formation
     * @return comprehensive public view DTO
     */
    @Transactional(readOnly = true)
    public FormationPublicViewDTO getPublishedFormationDetails(String formationId) {
        Formation formation = formationRepository.findByIdAndDeletedAtIsNull(formationId)
                .filter(f -> f.getStatus() == FormationStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published formation not found with ID: " + formationId));

        Artisan artisan = resolveAuthenticatedArtisan();

        boolean isAuthor = formation.getAuthor() != null && formation.getAuthor().getId().equals(artisan.getId());
        boolean isEnrolled = formationEnrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(),
                artisan.getId(),
                EnrollmentStatus.CONFIRMED
        );

        long activeEnrollmentsCount = formationEnrollmentRepository.countByFormationIdAndStatus(
                formation.getId(),
                EnrollmentStatus.CONFIRMED
        );

        List<FormationFile> activeFiles = formationFileRepository.findByFormationIdAndDeletedAtIsNull(formation.getId());

        return FormationPublicViewDTO.from(formation, activeFiles, activeEnrollmentsCount, isEnrolled, isAuthor);
    }

    /**
     * Enrolls the authenticated artisan into a published masterclass formation.
     * Validates that caller is not the author, seats remain within max capacity,
     * and reactivates previously cancelled registrations when applicable.
     *
     * @param formationId unique identifier of the formation
     * @return confirmed enrollment response DTO
     */
    @Transactional
    public FormationEnrollmentResponseDTO enrollInFormation(String formationId) {
        Artisan artisan = resolveAuthenticatedArtisan();

        Formation formation = formationRepository.findByIdAndDeletedAtIsNull(formationId)
                .filter(f -> f.getStatus() == FormationStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published formation not found with ID: " + formationId));

        if (formation.getAuthor() != null && formation.getAuthor().getId().equals(artisan.getId())) {
            throw new ConflictException("Authors cannot enroll in their own formations.");
        }

        long activeEnrollmentsCount = formationEnrollmentRepository.countByFormationIdAndStatus(formationId, EnrollmentStatus.CONFIRMED);
        if (activeEnrollmentsCount >= formation.getMaxParticipants()) {
            throw new ConflictException("Formation is fully booked. Maximum capacity reached.");
        }

        Optional<FormationEnrollment> existingOpt = formationEnrollmentRepository.findByFormationIdAndArtisanId(formationId, artisan.getId());
        FormationEnrollment enrollment;

        if (existingOpt.isPresent()) {
            FormationEnrollment existing = existingOpt.get();
            if (existing.getStatus() == EnrollmentStatus.CONFIRMED) {
                throw new ConflictException("You are already enrolled in this formation.");
            }
            if (existing.getStatus() == EnrollmentStatus.CANCELLED) {
                existing.setStatus(EnrollmentStatus.CONFIRMED);
                existing.setCancelledAt(null);
                existing.setEnrolledAt(LocalDateTime.now());
                enrollment = formationEnrollmentRepository.save(existing);
            } else {
                throw new ConflictException("Cannot enroll in this formation with status: " + existing.getStatus());
            }
        } else {
            FormationEnrollment newEnrollment = FormationEnrollment.builder()
                    .formation(formation)
                    .artisan(artisan)
                    .status(EnrollmentStatus.CONFIRMED)
                    .enrolledAt(LocalDateTime.now())
                    .build();
            enrollment = formationEnrollmentRepository.save(newEnrollment);
        }

        return FormationEnrollmentResponseDTO.from(enrollment);
    }

    /**
     * Cancels an active enrollment reservation for the authenticated artisan.
     * Enforces the configured cancellation cutoff deadline prior to scheduled masterclass start.
     *
     * @param formationId unique identifier of the formation
     * @return updated enrollment response DTO with CANCELLED status
     */
    @Transactional
    public FormationEnrollmentResponseDTO cancelEnrollment(String formationId) {
        Artisan artisan = resolveAuthenticatedArtisan();

        Formation formation = formationRepository.findByIdAndDeletedAtIsNull(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with ID: " + formationId));

        FormationEnrollment enrollment = formationEnrollmentRepository
                .findByFormationIdAndArtisanIdAndStatus(formationId, artisan.getId(), EnrollmentStatus.CONFIRMED)
                .orElseThrow(() -> new ResourceNotFoundException("Active confirmed enrollment not found for formation ID: " + formationId));

        int deadlineHours = appProperties.getFormation().getCancellation().getDeadlineHours();
        if (formation.getScheduledAt() != null) {
            LocalDateTime cutoff = formation.getScheduledAt().minusHours(deadlineHours);
            if (LocalDateTime.now().isAfter(cutoff)) {
                throw new BadRequestException("Cancellations must be made at least " + deadlineHours + " hours before the scheduled start time.");
            }
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(LocalDateTime.now());
        FormationEnrollment saved = formationEnrollmentRepository.save(enrollment);

        return FormationEnrollmentResponseDTO.from(saved);
    }

    /**
     * Retrieves paginated active and past enrollment history for the authenticated artisan.
     *
     * @param pageable pagination parameters
     * @return page of detailed enrollment records
     */
    @Transactional(readOnly = true)
    public Page<FormationEnrollmentDetailDTO> getMyEnrollments(Pageable pageable) {
        Artisan artisan = resolveAuthenticatedArtisan();

        Pageable effectivePageable = (pageable != null && pageable.getSort().isSorted())
                ? pageable
                : (pageable != null)
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "enrolledAt"))
                : PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "enrolledAt"));

        Page<FormationEnrollment> enrollments = formationEnrollmentRepository
                .findByArtisanIdAndDeletedAtIsNull(artisan.getId(), effectivePageable);

        return enrollments.map(enrollment -> {
            long activeCount = formationEnrollmentRepository.countByFormationIdAndStatus(
                    enrollment.getFormation().getId(),
                    EnrollmentStatus.CONFIRMED
            );
            return FormationEnrollmentDetailDTO.from(enrollment, activeCount);
        });
    }

    /**
     * Retrieves a protected course file attachment for download.
     * Access is restricted to confirmed enrolled participants and the authoring instructor.
     *
     * @param formationId unique identifier of the parent formation
     * @param fileId unique identifier of the course file
     * @return storage resource containing file data stream and metadata
     */
    @Transactional(readOnly = true)
    public StorageResource downloadCourseFile(String formationId, String fileId) {
        Artisan artisan = resolveAuthenticatedArtisan();

        Formation formation = formationRepository.findByIdAndDeletedAtIsNull(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with ID: " + formationId));

        FormationFile file = formationFileRepository.findByIdAndFormationIdAndDeletedAtIsNull(fileId, formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Course file not found with ID: " + fileId));

        boolean isAuthor = formation.getAuthor() != null && formation.getAuthor().getId().equals(artisan.getId());
        boolean isEnrolled = formationEnrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formationId,
                artisan.getId(),
                EnrollmentStatus.CONFIRMED
        );

        if (!isAuthor && !isEnrolled) {
            throw new ForbiddenException("Access denied: Course syllabus files are available only to confirmed enrolled participants.");
        }

        return storageService.load(file.getStorageKey());
    }

    /**
     * Delegates artisan identity resolution to the shared {@link ArtisanSecurityUtils} component.
     *
     * @return resolved Artisan entity for the current authenticated principal
     */
    private Artisan resolveAuthenticatedArtisan() {
        return ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository);
    }
}
