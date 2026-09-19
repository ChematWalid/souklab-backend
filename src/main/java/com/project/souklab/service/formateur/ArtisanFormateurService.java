package com.project.souklab.service.formateur;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanFormateurRequestRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formateur.FormateurApproveDTO;
import com.project.souklab.dto.formateur.FormateurCooldownOverrideDTO;
import com.project.souklab.dto.formateur.FormateurGrantDTO;
import com.project.souklab.dto.formateur.FormateurRejectDTO;
import com.project.souklab.dto.formateur.FormateurRequestDTO;
import com.project.souklab.dto.formateur.FormateurRequestResponseDTO;
import com.project.souklab.dto.formateur.FormateurRevokeDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanFormateurRequest;
import com.project.souklab.model.FormateurRequestStatus;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.util.EmailUtil;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtisanFormateurService {

    private static final String ERROR_ADMIN_NOT_FOUND_PREFIX = "Admin not found: ";
    private static final String ERROR_ARTISAN_NOT_FOUND_PREFIX = "Artisan not found with id: ";
    private static final String ERROR_REQUEST_NOT_FOUND_PREFIX = "Formateur request not found with id: ";
    private static final String ERROR_REQUEST_ALREADY_PREFIX = "Request is already ";

    private final ArtisanFormateurRequestRepository formateurRequestRepository;
    private final ArtisanRepository artisanRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailUtil emailUtil;
    private final AppProperties appProperties;
    private final Clock clock;

    /**
     * Submits a new formateur status request for the authenticated artisan.
     * Dual-dispatch to all admins: in-app + email.
     */
    @Transactional
    public FormateurRequestResponseDTO submitRequest(FormateurRequestDTO dto) {
        String email = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Your account must be ACTIVE to submit a Formateur request. Current status: " + user.getStatus());
        }

        Artisan profile = artisanRepository.findById(user.getId())
                .orElseThrow(() -> new ForbiddenException("Only registered artisans can request Formateur status."));

        if (profile.isTeacher()) {
            throw new ConflictException("You are already an approved Formateur.");
        }

        if (formateurRequestRepository.existsByArtisanAndStatusAndDeletedAtIsNull(profile, FormateurRequestStatus.PENDING)) {
            throw new ConflictException("You already have a pending Formateur request.");
        }

        Optional<ArtisanFormateurRequest> latestOpt = formateurRequestRepository.findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(profile);
        if (latestOpt.isPresent()) {
            ArtisanFormateurRequest latest = latestOpt.get();
            if (!latest.isCanReapply()) {
                throw new ForbiddenException("You are permanently blocked from submitting new Formateur requests.");
            }
            if (latest.getCooldownUntil() != null && latest.getCooldownUntil().isAfter(LocalDateTime.now(clock))) {
                throw new ForbiddenException("You cannot submit a request during the cooldown period. Cooldown expires on: " + latest.getCooldownUntil());
            }
        }

        ArtisanFormateurRequest request = ArtisanFormateurRequest.builder()
                .artisan(profile)
                .status(FormateurRequestStatus.PENDING)
                .motivation(dto != null ? dto.getMotivation() : null)
                .canReapply(true)
                .cooldownUntil(null)
                .build();

        ArtisanFormateurRequest saved = formateurRequestRepository.saveAndFlush(request);

        List<User> admins = userRepository.findByPermissionKey(Permission.Admin.USERS.value());
        String artisanName = resolveArtisanFullName(user);
        String notifMsg = "New artisan formateur request submitted by "
                + (artisanName != null && !artisanName.isBlank() ? artisanName + " (" + user.getEmail() + ")" : user.getEmail())
                + (dto != null && dto.getMotivation() != null && !dto.getMotivation().isBlank()
                        ? ": \"" + dto.getMotivation() + "\"" : "");
        for (User admin : admins) {
            notificationService.createForUser(admin, notifMsg, NotificationType.Formateur.REQUEST_SUBMITTED, saved.getId());
            emailUtil.sendFormateurRequestSubmittedNoticeToAdmin(admin.getEmail(), user.getEmail(), artisanName, dto != null ? dto.getMotivation() : null);
        }

        return mapToDTO(saved);
    }

    /**
     * Retrieves a paginated list of pending formateur requests for administrators.
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<FormateurRequestResponseDTO> getPendingRequests(Pageable pageable) {
        Page<ArtisanFormateurRequest> page = formateurRequestRepository
                .findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(FormateurRequestStatus.PENDING, pageable);
        return PaginatedResponse.from(page.map(this::mapToDTO));
    }

    /**
     * Approves a pending formateur request.
     */
    @Transactional
    public FormateurRequestResponseDTO approveRequest(String requestId, FormateurApproveDTO dto) {
        String adminEmail = SecurityUtils.getCurrentUsername();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ADMIN_NOT_FOUND_PREFIX + adminEmail));

        ArtisanFormateurRequest request = formateurRequestRepository.findByIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_REQUEST_NOT_FOUND_PREFIX + requestId));

        if (request.getStatus() != FormateurRequestStatus.PENDING) {
            throw new BadRequestException(ERROR_REQUEST_ALREADY_PREFIX + request.getStatus() + ".");
        }

        request.setStatus(FormateurRequestStatus.APPROVED);
        request.setAdminNote(dto.getAdminNote());
        request.setDecidedBy(admin);
        request.setDecidedAt(LocalDateTime.now(clock));

        Artisan artisan = request.getArtisan();
        artisan.setTeacher(true);
        artisanRepository.save(artisan);

        ArtisanFormateurRequest saved = formateurRequestRepository.saveAndFlush(request);

        User artisanUser = artisan.getUser();
        notificationService.createForUser(artisanUser, "Your request for Formateur status has been approved! Note: " + dto.getAdminNote(),
                NotificationType.Formateur.APPROVED, saved.getId());
        emailUtil.sendFormateurApprovedEmail(artisanUser.getEmail(), dto.getAdminNote());

        return mapToDTO(saved);
    }

    /**
     * Rejects a pending formateur request.
     */
    @Transactional
    public FormateurRequestResponseDTO rejectRequest(String requestId, FormateurRejectDTO dto) {
        String adminEmail = SecurityUtils.getCurrentUsername();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ADMIN_NOT_FOUND_PREFIX + adminEmail));

        ArtisanFormateurRequest request = formateurRequestRepository.findByIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_REQUEST_NOT_FOUND_PREFIX + requestId));

        if (request.getStatus() != FormateurRequestStatus.PENDING) {
            throw new BadRequestException(ERROR_REQUEST_ALREADY_PREFIX + request.getStatus() + ".");
        }

        boolean canReapply = dto.getCanReapply() == null || Boolean.TRUE.equals(dto.getCanReapply());
        LocalDateTime cooldownUntil = null;
        if (canReapply) {
            long cooldownDays = appProperties.getArtisan().getFormateur().getReapplyCooldownDays();
            cooldownUntil = dto.getCooldownUntil() != null ? dto.getCooldownUntil() : LocalDateTime.now(clock).plusDays(cooldownDays);
        }

        request.setStatus(FormateurRequestStatus.REJECTED);
        request.setAdminNote(dto.getAdminNote());
        request.setCanReapply(canReapply);
        request.setCooldownUntil(cooldownUntil);
        request.setDecidedBy(admin);
        request.setDecidedAt(LocalDateTime.now(clock));

        ArtisanFormateurRequest saved = formateurRequestRepository.saveAndFlush(request);

        User artisanUser = request.getArtisan().getUser();
        notificationService.createForUser(artisanUser, "Your Formateur request was rejected. Note: " + dto.getAdminNote(),
                NotificationType.Formateur.REJECTED, saved.getId());
        emailUtil.sendFormateurRejectedEmail(artisanUser.getEmail(), dto.getAdminNote(), cooldownUntil, canReapply);

        return mapToDTO(saved);
    }

    /**
     * Directly grants formateur status to an artisan without a prior request.
     */
    @Transactional
    public FormateurRequestResponseDTO grantDirectly(String artisanId, FormateurGrantDTO dto) {
        String adminEmail = SecurityUtils.getCurrentUsername();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ADMIN_NOT_FOUND_PREFIX + adminEmail));

        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ARTISAN_NOT_FOUND_PREFIX + artisanId));

        if (artisan.isTeacher()) {
            throw new BadRequestException("Artisan is already an approved Formateur.");
        }

        artisan.setTeacher(true);
        artisanRepository.save(artisan);

        ArtisanFormateurRequest auditRecord = ArtisanFormateurRequest.builder()
                .artisan(artisan)
                .status(FormateurRequestStatus.APPROVED)
                .motivation(null)
                .adminNote(dto.getAdminNote())
                .canReapply(true)
                .cooldownUntil(null)
                .decidedBy(admin)
                .decidedAt(LocalDateTime.now(clock))
                .build();

        ArtisanFormateurRequest saved = formateurRequestRepository.saveAndFlush(auditRecord);

        User artisanUser = artisan.getUser();
        notificationService.createForUser(artisanUser, "You have been granted Formateur status by an administrator! Note: " + dto.getAdminNote(),
                NotificationType.Formateur.GRANTED, saved.getId());
        emailUtil.sendFormateurGrantedEmail(artisanUser.getEmail(), dto.getAdminNote());

        return mapToDTO(saved);
    }

    /**
     * Directly revokes formateur status from an artisan.
     */
    @Transactional
    public void revokeDirectly(String artisanId, FormateurRevokeDTO dto) {
        String adminEmail = SecurityUtils.getCurrentUsername();
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ADMIN_NOT_FOUND_PREFIX + adminEmail));

        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ARTISAN_NOT_FOUND_PREFIX + artisanId));

        if (!artisan.isTeacher()) {
            throw new BadRequestException("Artisan is not currently an approved Formateur.");
        }

        artisan.setTeacher(false);
        artisanRepository.save(artisan);

        User artisanUser = artisan.getUser();
        notificationService.createForUser(artisanUser, "Your Formateur status has been revoked. Reason: " + dto.getReason(),
                NotificationType.Formateur.REVOKED, artisanId);
        emailUtil.sendFormateurRevokedEmail(artisanUser.getEmail(), dto.getReason());
    }

    /**
     * Administratively overrides/lifts the cooldown or reapply block on an artisan's latest request record.
     */
    @Transactional
    public FormateurRequestResponseDTO liftCooldown(String artisanId, FormateurCooldownOverrideDTO dto) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ARTISAN_NOT_FOUND_PREFIX + artisanId));

        ArtisanFormateurRequest request = formateurRequestRepository
                .findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(artisan)
                .orElseThrow(() -> new ResourceNotFoundException("No Formateur request record found for artisan ID: " + artisanId));

        if (dto.getCanReapply() != null) {
            request.setCanReapply(dto.getCanReapply());
        }
        if (dto.getCooldownUntil() != null) {
            request.setCooldownUntil(dto.getCooldownUntil());
        } else if (Boolean.TRUE.equals(dto.getCanReapply())) {
            request.setCooldownUntil(null);
        }

        ArtisanFormateurRequest saved = formateurRequestRepository.saveAndFlush(request);
        return mapToDTO(saved);
    }

    private FormateurRequestResponseDTO mapToDTO(ArtisanFormateurRequest req) {
        User user = req.getArtisan() != null ? req.getArtisan().getUser() : null;
        String artisanName = resolveArtisanFullName(user);

        return FormateurRequestResponseDTO.builder()
                .id(req.getId())
                .artisanId(req.getArtisan() != null ? req.getArtisan().getId() : null)
                .artisanName(artisanName)
                .artisanEmail(user != null ? user.getEmail() : null)
                .status(req.getStatus())
                .motivation(req.getMotivation())
                .adminNote(req.getAdminNote())
                .canReapply(req.isCanReapply())
                .cooldownUntil(req.getCooldownUntil())
                .decidedByAdminId(req.getDecidedBy() != null ? req.getDecidedBy().getId() : null)
                .decidedByAdminEmail(req.getDecidedBy() != null ? req.getDecidedBy().getEmail() : null)
                .decidedAt(req.getDecidedAt())
                .createdAt(req.getCreatedAt())
                .build();
    }

    private static String resolveArtisanFullName(User user) {
        if (user == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (user.getFirstName() != null) {
            sb.append(user.getFirstName()).append(" ");
        }
        if (user.getLastName() != null) {
            sb.append(user.getLastName());
        }
        return sb.toString().trim();
    }
}
