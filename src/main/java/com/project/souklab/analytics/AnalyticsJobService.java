package com.project.souklab.analytics;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.config.AnalyticsProperties;
import com.project.souklab.config.AnalyticsJobProperties;
import com.project.souklab.config.AnalyticsExportProperties;
import com.project.souklab.config.AppProperties;
import com.project.souklab.config.OperationalMetrics;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.AnalyticsJobRepository;
import com.project.souklab.dao.analytics.AnalyticsJobArtifactRepository;
import com.project.souklab.dao.analytics.AnalyticsOutboxRepository;
import com.project.souklab.dao.analytics.AnalyticsMaintenanceJobRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.FormationRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.ContentReportRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.ArtisanFormateurRequestRepository;
import com.project.souklab.dto.analytics.AnalyticsJobRequest;
import com.project.souklab.dto.analytics.AnalyticsJobResponse;
import com.project.souklab.dto.analytics.AnalyticsResult;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.FormateurRequestStatus;
import com.project.souklab.model.ReviewStatus;
import com.project.souklab.model.analytics.AnalyticsJob;
import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsJobArtifact;
import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsReportType;
import com.project.souklab.model.analytics.AnalyticsFilterKey;
import com.project.souklab.model.analytics.AnalyticsOutputFormat;
import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import com.project.souklab.model.analytics.OutboxStatus;
import com.project.souklab.dto.analytics.AnalyticsJobEvent;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.ReportStatus;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.SubscriptionStatus;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.security.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsJobService {
    private final AnalyticsJobRepository jobs;
    private final ActivityEventRepository events;
    private final UserRepository users;
    private final AnalyticsProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppProperties appProperties;
    private final FeedPostRepository feedPosts;
    private final FormationRepository formations;
    private final FormationEnrollmentRepository enrollments;
    private final ArtisanReviewRepository reviews;
    private final ContentReportRepository reports;
    private final PaymentRepository payments;
    private final AnalyticsJobArtifactRepository artifacts;
    private final StorageService storageService;
    private final AuditLogService auditLogService;
    private final AnalyticsOutboxRepository outbox;
    private final AnalyticsJobProperties jobProperties;
    private final AnalyticsExportProperties exportProperties;
    private final ArtisanSubscriptionRepository artisanSubscriptions;
    private final ClientSubscriptionRepository clientSubscriptions;
    private final ArtisanFormateurRequestRepository formateurRequests;
    @Qualifier("applicationTaskExecutor")
    private final Executor applicationTaskExecutor;
    private final TransactionTemplate transactionTemplate;
    private OperationalMetrics operationalMetrics;
    private Semaphore concurrencyLimiter;
    private HealthEndpoint healthEndpoint;
    private AnalyticsMaintenanceJobRepository maintenanceJobs;

    @Autowired(required = false)
    void setHealthEndpoint(HealthEndpoint value) { this.healthEndpoint = value; }

    @Autowired(required = false)
    void setMaintenanceJobs(AnalyticsMaintenanceJobRepository value) { this.maintenanceJobs = value; }

    @Autowired(required = false)
    void setOperationalMetrics(OperationalMetrics value) { this.operationalMetrics = value; }

    @PostConstruct
    void initializeConcurrencyLimiter() {
        concurrencyLimiter = new Semaphore(jobProperties.getConcurrency());
    }

    @Transactional
    public AnalyticsJobResponse submit(AnalyticsJobRequest request, String username, boolean financial) {
        validate(request, financial);
        String ownerId = users.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated administrator not found")).getId();
        AnalyticsJob job = new AnalyticsJob();
        job.setOwnerId(ownerId);
        job.setReportType(request.getReportType());
        job.setBucket(request.getBucket());
        job.setFromDate(request.getFromDate()); job.setToDate(request.getToDate());
        job.setPageNumber(request.getPageNumber() == null ? 0 : request.getPageNumber());
        job.setPageSize(request.getPageSize() == null ? properties.getDefaultPageSize() : request.getPageSize());
        job.setSortField(request.getSortField()); job.setSortDirection(request.getSortDirection());
        job.setOutputFormat(request.getOutputFormat() == null ? AnalyticsOutputFormat.JSON : request.getOutputFormat());
        job.setStatus(AnalyticsJobStatus.QUEUED);
        job.setPermissionScope(financial
                ? Permission.Analytics.ADMIN.value() + "," + Permission.Financial.ADMIN.value()
                : Permission.Analytics.ADMIN.value());
        try { job.setFiltersJson(objectMapper.writeValueAsString(request.getFilters() == null ? Map.of() : request.getFilters())); }
        catch (JsonProcessingException e) { throw new BadRequestException("Invalid analytics filters", e); }
        job.setExpiresAt(LocalDateTime.now(clock).plus(properties.getJobRetention()));
        AnalyticsJob saved = jobs.saveAndFlush(job);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            String jobId = saved.getId();
            TransactionSynchronizationManager.registerSynchronization(new AnalyticsJobCommitCallback(jobId, this));
        } else {
            processAsync(saved.getId());
        }
        audit(AuditLogAction.ANALYTICS_JOB_SUBMITTED, saved, "outcome=ACCEPTED");
        return response(saved);
    }

    public void processAsync(String id) {
        applicationTaskExecutor.execute(() -> transactionTemplate.executeWithoutResult(status -> processJob(id)));
    }

    private void processJob(String id) {
        concurrencyLimiter.acquireUninterruptibly();
        try {
            jobs.findById(id).ifPresent(job -> {
            try {
                long startedAt = System.nanoTime();
                job.setStatus(AnalyticsJobStatus.RUNNING);
                jobs.save(job);
                Map<String, Object> summary = new LinkedHashMap<>();
                LocalDateTime from = utcStart(job.getFromDate());
                LocalDateTime to = utcStart(job.getToDate().plusDays(1));
                LocalDateTime inclusiveTo = to.minusNanos(1);
                summary.put("totalUsers", users.countByDeletedAtIsNull());
                summary.put("newRegistrations", users.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                summary.put("verifiedRegistrations", users.countByEmailVerifiedTrueAndCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                long registrations = (long) summary.get("newRegistrations");
                long verifiedRegistrations = (long) summary.get("verifiedRegistrations");
                summary.put("activationRate", registrations == 0 ? 0.0 : (double) verifiedRegistrations / registrations);
                summary.put("verifiedUsers", users.countByEmailVerifiedTrueAndDeletedAtIsNull());
                summary.put("artisanProfiles", users.countArtisanProfiles());
                summary.put("clientProfiles", users.countClientProfiles());
                summary.put("activeArtisanProfiles", users.countActiveArtisanProfiles(AccountStatus.ACTIVE));
                summary.put("activeClientProfiles", users.countActiveClientProfiles(AccountStatus.ACTIVE));
                summary.put("activeUsers", users.countByStatusAndDeletedAtIsNull(AccountStatus.ACTIVE));
                summary.put("pendingUsers", users.countByStatusAndDeletedAtIsNull(AccountStatus.PENDING));
                summary.put("suspendedUsers", users.countByStatusAndDeletedAtIsNull(AccountStatus.SUSPENDED));
                summary.put("pendingUserApprovals", users.countByStatusAndDeletedAtIsNull(AccountStatus.PENDING));
                Map<String, Long> moderationActivity = new LinkedHashMap<>();
                for (AnalyticsEvent.Type eventType : List.of(AnalyticsEvent.User.APPROVED, AnalyticsEvent.User.SUSPENDED,
                        AnalyticsEvent.User.TIMED_OUT, AnalyticsEvent.User.REINSTATED,
                        AnalyticsEvent.Formation.Moderation.APPROVED, AnalyticsEvent.Formation.Moderation.REJECTED,
                        AnalyticsEvent.Report.RESOLVED)) {
                    moderationActivity.put(eventType.value(), countFilteredEvent(job, eventType, from, inclusiveTo));
                }
                summary.put("moderationActivity", moderationActivity);
                Map<String, Long> userStatuses = new LinkedHashMap<>();
                for (AccountStatus status : AccountStatus.values()) {
                    userStatuses.put(status.value(), users.countByStatusAndDeletedAtIsNull(status));
                }
                summary.put("userStatuses", userStatuses);
                summary.put("activityEvents", countFilteredEvents(job, from, inclusiveTo));
                summary.put("successfulLogins", countFilteredEvent(job, AnalyticsEvent.Authentication.Login.SUCCEEDED, from, inclusiveTo));
                summary.put("publishedPosts", countFilteredEvent(job, AnalyticsEvent.Feed.Post.PUBLISHED, from, inclusiveTo));
                summary.put("messagesSent", countFilteredEvent(job, AnalyticsEvent.Message.SENT, from, inclusiveTo));
                LocalDateTime activityDayStart = utcStart(job.getToDate());
                LocalDateTime activityWeekStart = utcStart(job.getToDate().minusDays(6));
                LocalDateTime activityMonthStart = utcStart(job.getToDate().minusDays(29));
                summary.put("dau", countFilteredDistinctActors(job, activityDayStart, inclusiveTo));
                summary.put("wau", countFilteredDistinctActors(job, activityWeekStart, inclusiveTo));
                summary.put("mau", countFilteredDistinctActors(job, activityMonthStart, inclusiveTo));
                AnalyticsEvent.Type eventFilter = eventTypeFilter(job);
                summary.put("engagementByAccountType", Map.of(
                        "artisan", eventFilter == null
                                ? events.countDistinctArtisanActorsByEventTimeBetween(from, inclusiveTo)
                                : events.countDistinctArtisanActorsByTypeAndEventTimeBetween(eventFilter, from, inclusiveTo),
                        "client", eventFilter == null
                                ? events.countDistinctClientActorsByEventTimeBetween(from, inclusiveTo)
                                : events.countDistinctClientActorsByTypeAndEventTimeBetween(eventFilter, from, inclusiveTo)));
                if (job.getReportType() == AnalyticsReportType.GROWTH) {
                    summary.put("loginRetentionCohorts", loginRetentionCohorts(from, inclusiveTo));
                }
                if (feedPosts != null) summary.put("feedPostsCreated", feedPosts.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (formations != null) summary.put("formationsCreated", formations.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (enrollments != null) summary.put("formationEnrollments", enrollments.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (reviews != null) summary.put("reviewsSubmitted", reviews.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (reviews != null) {
                    summary.put("publishedReviews", reviews.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                            ReviewStatus.PUBLISHED, from, inclusiveTo));
                    BigDecimal averageRating = reviews.averageRatingByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                            ReviewStatus.PUBLISHED, from, inclusiveTo);
                    summary.put("averagePublishedRating", averageRating == null ? BigDecimal.ZERO : averageRating);
                }
                if (reports != null) summary.put("reportsSubmitted", reports.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                summary.put("formateurPending", formateurRequests.countByStatusAndDeletedAtIsNull(FormateurRequestStatus.PENDING));
                summary.put("formateurApprovedInRange", formateurRequests.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                        FormateurRequestStatus.APPROVED, from, inclusiveTo));
                summary.put("formateurRejectedInRange", formateurRequests.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                        FormateurRequestStatus.REJECTED, from, inclusiveTo));
                Map<String, Long> formateurStatuses = new LinkedHashMap<>();
                for (FormateurRequestStatus status : FormateurRequestStatus.values()) {
                    formateurStatuses.put(status.value(), formateurRequests.countByStatusAndDeletedAtIsNull(status));
                }
                summary.put("formateurStatuses", formateurStatuses);
                if (payments != null) summary.put("paymentsCreated", payments.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (feedPosts != null) {
                    Map<String, Long> statuses = new LinkedHashMap<>();
                    for (FeedPostStatus status : FeedPostStatus.values()) statuses.put(status.value(), feedPosts.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put("feedPostsByStatus", statuses);
                }
                if (formations != null) {
                    Map<String, Long> statuses = new LinkedHashMap<>();
                    for (FormationStatus status : FormationStatus.values()) statuses.put(status.value(), formations.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put("formationsByStatus", statuses);
                }
                if (enrollments != null) {
                    Map<String, Long> statuses = new LinkedHashMap<>();
                    for (EnrollmentStatus status : EnrollmentStatus.values()) statuses.put(status.value(), enrollments.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put("enrollmentsByStatus", statuses);
                    long enrollmentTotal = statuses.values().stream().mapToLong(Long::longValue).sum();
                    summary.put("formationCompletions", statuses.getOrDefault(EnrollmentStatus.ATTENDED.value(), 0L));
                    summary.put("enrollmentCancellationRate", enrollmentTotal == 0 ? 0.0
                            : (double) statuses.getOrDefault(EnrollmentStatus.CANCELLED.value(), 0L) / enrollmentTotal);
                }
                if (reports != null) {
                    Map<String, Long> statuses = new LinkedHashMap<>();
                    for (ReportStatus status : ReportStatus.values()) statuses.put(status.value(), reports.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put("reportsByStatus", statuses);
                }
                if (payments != null) {
                    Map<String, Long> statuses = new LinkedHashMap<>();
                        for (PaymentStatus status : PaymentStatus.values()) {
                        statuses.put(status.value(), payments.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    }
                    summary.put("paymentsByStatus", statuses);
                    if (job.getReportType() == AnalyticsReportType.SUBSCRIPTIONS_PAYMENTS) {
                        Map<String, Long> subscriptions = new LinkedHashMap<>();
                        for (SubscriptionStatus status : SubscriptionStatus.values()) {
                            subscriptions.put(status.value(), artisanSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo)
                                    + clientSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                        }
                        summary.put("subscriptionsByStatus", subscriptions);
                        Map<String, Map<String, Long>> subscriptionsBySubscriberType = new LinkedHashMap<>();
                        Map<String, Long> artisanSubscriptionStatuses = new LinkedHashMap<>();
                        Map<String, Long> clientSubscriptionStatuses = new LinkedHashMap<>();
                            for (SubscriptionStatus status : SubscriptionStatus.values()) {
                            artisanSubscriptionStatuses.put(status.value(), artisanSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                            clientSubscriptionStatuses.put(status.value(), clientSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                        }
                        subscriptionsBySubscriberType.put("artisan", artisanSubscriptionStatuses);
                        subscriptionsBySubscriberType.put("client", clientSubscriptionStatuses);
                        summary.put("subscriptionsBySubscriberType", subscriptionsBySubscriberType);
                        Map<String, Long> lifecycleEvents = new LinkedHashMap<>();
                        for (AnalyticsEvent.Type eventType : List.of(AnalyticsEvent.Subscription.ACTIVATED, AnalyticsEvent.Subscription.EXPIRED,
                                AnalyticsEvent.Subscription.CANCELED, AnalyticsEvent.Subscription.REVOKED, AnalyticsEvent.Subscription.RENEWAL)) {
                            lifecycleEvents.put(eventType.value(), countFilteredEvent(job, eventType, from, inclusiveTo));
                        }
                        summary.put("subscriptionLifecycleEvents", lifecycleEvents);
                        summary.put("checkoutCreated", countFilteredEvent(job, AnalyticsEvent.Checkout.CREATED, from, inclusiveTo));
                        summary.put("paymentStateTransitions", countFilteredEvent(job, AnalyticsEvent.Payment.State.TRANSITION, from, inclusiveTo));
                        long grossCollected = payments.sumAmountByStatusAndCurrencyAndCreatedAtBetween(
                                PaymentStatus.PAID, "DZD", from, to);
                        long providerFees = payments.sumFeesByStatusAndCurrencyAndCreatedAtBetween(
                                PaymentStatus.PAID, "DZD", from, to);
                        summary.put("grossCollectedDzd", grossCollected);
                        summary.put("providerFeesDzd", providerFees);
                        summary.put("netCollectedDzd", grossCollected - providerFees);
                        long paidPayments = payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.PAID, from, inclusiveTo);
                        long failedPayments = payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.FAILED, from, inclusiveTo)
                                + payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.CANCELED, from, inclusiveTo)
                                + payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.EXPIRED, from, inclusiveTo);
                        summary.put("paymentConversionRate", paidPayments + failedPayments == 0 ? 0.0
                                : (double) paidPayments / (paidPayments + failedPayments));
                        summary.put("manualGrants", payments.countByManualGrantTrueAndCreatedAtBetweenAndDeletedAtIsNull(
                                from, inclusiveTo));
                    }
                }
                if (job.getReportType() == AnalyticsReportType.OPERATIONAL) {
                    Map<String, Object> operational = new LinkedHashMap<>();
                    operational.put("analyticsJobsQueued", jobs.countByStatus(AnalyticsJobStatus.QUEUED));
                    operational.put("analyticsJobsRunning", jobs.countByStatus(AnalyticsJobStatus.RUNNING));
                    operational.put("analyticsJobsCompleted", jobs.countByStatus(AnalyticsJobStatus.COMPLETED));
                    operational.put("analyticsJobsFailed", jobs.countByStatus(AnalyticsJobStatus.FAILED));
                    if (maintenanceJobs != null) {
                        operational.put("maintenanceJobsQueued", maintenanceJobs.countByStatus(AnalyticsJobStatus.QUEUED));
                        operational.put("maintenanceJobsRunning", maintenanceJobs.countByStatus(AnalyticsJobStatus.RUNNING));
                        operational.put("maintenanceJobsCompleted", maintenanceJobs.countByStatus(AnalyticsJobStatus.COMPLETED));
                        operational.put("maintenanceJobsFailed", maintenanceJobs.countByStatus(AnalyticsJobStatus.FAILED));
                    }
                    operational.put("outboxPending", outbox.countByStatus(OutboxStatus.PENDING));
                    operational.put("outboxPublished", outbox.countByStatus(OutboxStatus.PUBLISHED));
                    operational.put("outboxDeadLetter", outbox.countByStatus(OutboxStatus.DEAD_LETTER));
                    if (healthEndpoint != null) {
                        HealthDescriptor health = healthEndpoint.health();
                        operational.put("applicationHealth", health.getStatus().getCode());
                        operational.put("healthComponents", healthComponentStatuses(health));
                    }
                    if (operationalMetrics != null) {
                        operational.put("requestCounters", operationalMetrics.snapshot("souklab.http.requests"));
                        operational.put("rateLimitRejections", operationalMetrics.snapshot("souklab.rate_limit.rejections"));
                    }
                    summary.put("operational", operational);
                }
                long rangeDays = ChronoUnit.DAYS.between(job.getFromDate(), job.getToDate()) + 1;
                LocalDateTime previousFrom = from.minusDays(rangeDays);
                LocalDateTime previousTo = from.minusNanos(1);
                Map<String, Object> comparison = new LinkedHashMap<>();
                comparison.put("newRegistrations", Map.of(
                        "current", users.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo),
                        "previous", users.countByCreatedAtBetweenAndDeletedAtIsNull(previousFrom, previousTo)));
                comparison.put("activityEvents", Map.of("current", countFilteredEvents(job, from, inclusiveTo), "previous", countFilteredEvents(job, previousFrom, previousTo)));
                summary.put("periodComparison", comparison);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("reportType", job.getReportType());
                result.put("fromDate", job.getFromDate());
                result.put("toDate", job.getToDate());
                result.put("bucket", job.getBucket());
                result.put("summary", summary);
                result.put("tables", buildTables(job, summary));
                List<Map<String, Object>> allSeries = buildSeries(job, from, to);
                sortSeries(allSeries, job);
                if (allSeries.size() > jobProperties.getMaximumResultRows()) {
                    throw new BadRequestException("Analytics result exceeds configured row limit");
                }
                if (Duration.ofNanos(System.nanoTime() - startedAt).compareTo(properties.getQueryTimeout()) > 0) {
                    throw new BadRequestException("Analytics query exceeded configured timeout");
                }
                long requestedPageStart = (long) job.getPageNumber() * job.getPageSize();
                int pageStart = requestedPageStart >= allSeries.size()
                        ? allSeries.size() : (int) requestedPageStart;
                int pageEnd = Math.min(pageStart + job.getPageSize(), allSeries.size());
                result.put("series", PaginatedResponse.<Map<String, Object>>builder()
                        .content(allSeries.subList(pageStart, pageEnd))
                        .pageNumber(job.getPageNumber())
                        .pageSize(job.getPageSize())
                        .totalElements(allSeries.size())
                        .totalPages(allSeries.isEmpty() ? 0 : (allSeries.size() + job.getPageSize() - 1) / job.getPageSize())
                        .last(pageEnd >= allSeries.size())
                        .build());
                String serializedResult = objectMapper.writeValueAsString(result);
                if (serializedResult.getBytes(StandardCharsets.UTF_8).length > jobProperties.getMaximumResultBytes()) {
                    throw new BadRequestException("Analytics result exceeds configured byte limit");
                }
                job.setResultJson(serializedResult);
                materializeArtifact(job, serializedResult);
                job.setStatus(AnalyticsJobStatus.COMPLETED);
                job.setCompletedAt(LocalDateTime.now(clock));
                jobs.save(job);
                notifyOwner(job, null);
            } catch (Exception ex) {
                log.error("Analytics job {} failed", id, ex);
                job.setStatus(AnalyticsJobStatus.FAILED);
                job.setFailureMessage(ex.getMessage()); jobs.save(job);
                notifyOwner(job, ex.getMessage());
            }
            });
        } finally {
            concurrencyLimiter.release();
        }
    }

    private Map<String, String> healthComponentStatuses(HealthDescriptor descriptor) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (descriptor instanceof CompositeHealthDescriptor composite) {
            composite.getComponents().forEach((name, component) ->
                    statuses.put(name, component.getStatus().getCode()));
        }
        return statuses;
    }

    private void notifyOwner(AnalyticsJob job, String failure) {
        if (messagingTemplate == null || appProperties == null) return;
        users.findById(job.getOwnerId()).ifPresent(owner -> messagingTemplate.convertAndSendToUser(
                owner.getEmail(), appProperties.getChat().getNotificationDestination(),
                new AnalyticsJobEvent(job.getId(), job.getStatus(), job.getReportType(), failure)));
    }

    @Transactional(readOnly = true)
    public AnalyticsJobResponse get(String id, String username) {
        return response(ownerJob(id, username));
    }

    @Transactional(readOnly = true)
    public AnalyticsResult result(String id, String username) {
        AnalyticsJob job = ownerJob(id, username);
        if (job.getStatus() != AnalyticsJobStatus.COMPLETED || job.getResultJson() == null) {
            throw new BadRequestException("Analytics result is not ready");
        }
        audit(AuditLogAction.ANALYTICS_RESULT_READ, job, "outcome=SUCCESS");
        try {
            return objectMapper.readValue(job.getResultJson(), AnalyticsResult.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Analytics result is not available", ex);
        }
    }

    @Transactional(readOnly = true)
    public Download download(String id, String username) {
        AnalyticsJob job = ownerJob(id, username);
        if (job.getStatus() != AnalyticsJobStatus.COMPLETED || job.getResultJson() == null) {
            throw new BadRequestException("Analytics result is not ready");
        }
        audit(AuditLogAction.ANALYTICS_EXPORT, job, "outcome=SUCCESS");
        AnalyticsJobArtifact artifact = artifacts.findFirstByJobIdOrderByCreatedAtDesc(job.getId()).orElse(null);
        if (artifact != null && storageService != null) {
            try {
                StorageResource resource = storageService.retrieve(artifact.getStorageKey());
                return new Download(new String(resource.content().readAllBytes(), StandardCharsets.UTF_8),
                        "text/csv".equals(artifact.getContentType()));
            } catch (IOException e) { throw new BadRequestException("Analytics artifact is not available", e); }
        }
        if (job.getOutputFormat() != AnalyticsOutputFormat.CSV) return new Download(job.getResultJson(), false);
        try {
            return new Download(toCsv(job.getResultJson()), true);
        } catch (Exception e) { throw new BadRequestException("Analytics result is not available", e); }
    }

    private void materializeArtifact(AnalyticsJob job, String json) {
        if (storageService == null) return;
        boolean csv = job.getOutputFormat() == AnalyticsOutputFormat.CSV;
        byte[] content = (csv ? toCsv(json) : json).getBytes(StandardCharsets.UTF_8);
        String filename = exportProperties.getStoragePrefix() + "/analytics-" + job.getId() + (csv ? ".csv" : ".json");
        var stored = storageService.store(new ByteArrayInputStream(content), filename,
                csv ? "text/csv" : "application/json", content.length);
        AnalyticsJobArtifact artifact = new AnalyticsJobArtifact();
        artifact.setJobId(job.getId()); artifact.setStorageKey(stored.key()); artifact.setFileName(filename);
        artifact.setContentType(csv ? "text/csv" : "application/json"); artifact.setSize(content.length);
        artifact.setSha256(sha256(content)); artifact.setExpiresAt(LocalDateTime.now(clock).plus(exportProperties.getRetention()));
        artifacts.save(artifact);
    }

    private String toCsv(String json) {
        try {
            var root = objectMapper.readTree(json);
            var summary = root.path("summary");
            var series = root.path("series").path("content");
            StringBuilder csv = new StringBuilder("section,key,value\n");
            summary.fields().forEachRemaining(e -> csv.append(csvCell("summary")).append(',')
                    .append(csvCell(e.getKey())).append(',')
                    .append(csvCell(e.getValue().isContainerNode() ? e.getValue().toString() : e.getValue().asText()))
                    .append('\n'));
            for (int index = 0; index < series.size(); index++) {
                final int pointIndex = index;
                var point = series.get(index);
                point.fields().forEachRemaining(e -> csv.append(csvCell("series[" + pointIndex + "]")).append(',')
                        .append(csvCell(e.getKey())).append(',')
                        .append(csvCell(e.getValue().isContainerNode() ? e.getValue().toString() : e.getValue().asText()))
                        .append('\n'));
            }
            var tables = root.path("tables");
            tables.fields().forEachRemaining(table -> {
                var content = table.getValue().path("content");
                for (int index = 0; index < content.size(); index++) {
                    var row = content.get(index);
                    row.fields().forEachRemaining(entry -> csv.append(csvCell("table." + table.getKey()))
                            .append(',').append(csvCell(entry.getKey())).append(',')
                            .append(csvCell(entry.getValue().isContainerNode()
                                    ? entry.getValue().toString() : entry.getValue().asText()))
                            .append('\n'));
                }
            });
            return csv.toString();
        } catch (JsonProcessingException e) { throw new BadRequestException("Analytics result is not available", e); }
    }

    private String csvCell(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String sha256(byte[] content) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 is unavailable", e); }
    }

    public record Download(String content, boolean csv) { }

    @Transactional
    public void delete(String id, String username) {
        AnalyticsJob job = ownerJob(id, username);
        artifacts.findFirstByJobIdOrderByCreatedAtDesc(job.getId()).ifPresent(artifact -> {
            if (storageService != null) {
                try { storageService.delete(artifact.getStorageKey()); }
                catch (RuntimeException ex) { log.warn("Could not remove analytics artifact {}", artifact.getStorageKey(), ex); }
            }
            artifacts.delete(artifact);
        });
        jobs.delete(job);
    }

    private AnalyticsJob ownerJob(String id, String username) {
        String owner = users.findByEmail(username).map(u -> u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated administrator not found"));
        AnalyticsJob job = jobs.findByIdAndOwnerId(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics job not found"));
        return job;
    }

    private void audit(AuditLogAction action, AnalyticsJob job, String outcome) {
        if (auditLogService == null) return;
        auditLogService.logAction(action, auditDetails(job, outcome));
    }

    private String auditDetails(AnalyticsJob job, String outcome) {
        return "jobId=" + job.getId()
                + ",reportType=" + job.getReportType()
                + ",range=" + job.getFromDate() + ".." + job.getToDate()
                + ",filters=" + (job.getFiltersJson() == null ? "{}" : job.getFiltersJson())
                + ",permissionScope=" + job.getPermissionScope()
                + ",outcome=" + outcome;
    }

    private void validate(AnalyticsJobRequest r, boolean financial) {
        if (r.getReportType() == null || r.getBucket() == null || r.getFromDate() == null || r.getToDate() == null) throw new BadRequestException("Report type, range, and bucket are required");
        if (r.getToDate().isBefore(r.getFromDate())) throw new BadRequestException("Analytics end date must not precede start date");
        long days = ChronoUnit.DAYS.between(r.getFromDate(), r.getToDate()) + 1;
        if (days > properties.getMaximumRangeDays()) throw new BadRequestException("Analytics date range exceeds configured maximum");
        long estimatedBuckets = switch (r.getBucket()) {
            case DAY -> days;
            case WEEK -> (days + 6) / 7;
            case MONTH -> (days + 30) / 31;
            case QUARTER -> (days + 89) / 90;
        };
        if (estimatedBuckets > properties.getMaximumBucketCount()) throw new BadRequestException("Analytics bucket count exceeds configured maximum");
        int pageSize = r.getPageSize() == null ? properties.getDefaultPageSize() : r.getPageSize();
        if (pageSize < 1 || pageSize > properties.getMaximumPageSize()) throw new BadRequestException("Analytics page size is outside configured bounds");
        if (r.getPageNumber() != null && r.getPageNumber() < 0) throw new BadRequestException("Analytics page number must not be negative");
        if (r.getReportType() == AnalyticsReportType.SUBSCRIPTIONS_PAYMENTS && !financial) {
            throw new ForbiddenException("Financial analytics permission is required");
        }
        if (r.getOutputFormat() != null && r.getOutputFormat() != AnalyticsOutputFormat.JSON
                && r.getOutputFormat() != AnalyticsOutputFormat.CSV) throw new BadRequestException("Output format must be JSON or CSV");
        if (r.getReportType() == AnalyticsReportType.CSV_EXPORT
                && r.getOutputFormat() != AnalyticsOutputFormat.CSV) {
            throw new BadRequestException("CSV_EXPORT reports require CSV output format");
        }
        if (r.getFilters() != null && r.getFilters().keySet().stream()
                .anyMatch(key -> AnalyticsFilterKey.fromKey(key).isEmpty())) {
            throw new BadRequestException("Unsupported analytics filter");
        }
        if (r.getFilters() != null && r.getFilters().containsKey(AnalyticsFilterKey.EVENT_TYPE.key())
                && AnalyticsEvent.fromValue(r.getFilters().get(AnalyticsFilterKey.EVENT_TYPE.key())).isEmpty()) {
            throw new BadRequestException("Unsupported analytics event type filter");
        }
    }

    private List<Map<String, Object>> buildSeries(AnalyticsJob job, LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> series = new ArrayList<>();
        Map<String, String> filters = readFilters(job);
        LocalDate cursor = firstBucketDate(job.getFromDate(), job.getBucket());
        while (!cursor.isAfter(job.getToDate())) {
            LocalDate next = switch (job.getBucket()) {
                case DAY -> cursor.plusDays(1);
                case WEEK -> cursor.plusWeeks(1);
                case MONTH -> cursor.with(TemporalAdjusters.firstDayOfNextMonth());
                case QUARTER -> cursor.plusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
            };
            LocalDate start = cursor.isBefore(job.getFromDate()) ? job.getFromDate() : cursor;
            LocalDate end = next.minusDays(1).isAfter(job.getToDate()) ? job.getToDate() : next.minusDays(1);
            LocalDateTime bucketFrom = utcStart(start);
            LocalDateTime bucketTo = utcStart(end.plusDays(1));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("startDate", start); point.put("endDate", end);
            AnalyticsEvent.Type eventType = AnalyticsEvent.fromValue(filters.get(AnalyticsFilterKey.EVENT_TYPE.key())).orElse(null);
            LocalDateTime inclusiveBucketTo = bucketTo.minusNanos(1);
            point.put("activityEvents", eventType == null
                    ? events.countByEventTimeBetween(bucketFrom, inclusiveBucketTo)
                    : events.countByEventTypeAndEventTimeBetween(eventType, bucketFrom, inclusiveBucketTo));
            point.put("uniqueActors", eventType == null
                    ? events.countDistinctActorsByEventTimeBetween(bucketFrom, inclusiveBucketTo)
                    : events.countDistinctActorsByTypeAndEventTimeBetween(eventType, bucketFrom, inclusiveBucketTo));
            point.put("newRegistrations", users.countByCreatedAtBetweenAndDeletedAtIsNull(bucketFrom, inclusiveBucketTo));
            series.add(point);
            cursor = next;
        }
        return series;
    }

    private void sortSeries(List<Map<String, Object>> series, AnalyticsJob job) {
        AnalyticsSeriesSorter.sort(series, job.getSortField(), job.getSortDirection());
    }

    private List<Map<String, Object>> loginRetentionCohorts(LocalDateTime from, LocalDateTime to) {
        Map<String, LocalDateTime> firstRegistrationByActor = new LinkedHashMap<>();
        for (var event : events.findByEventTypeAndEventTimeBetweenOrderByEventTimeAsc(
                AnalyticsEvent.Registration.CREATED, from, to)) {
            if (event.getActorId() != null) {
                firstRegistrationByActor.putIfAbsent(event.getActorId(), event.getEventTime());
            }
        }
        if (firstRegistrationByActor.isEmpty()) return List.of();
        Map<String, Set<String>> retainedByWindow = new LinkedHashMap<>();
        retainedByWindow.put("day1", new HashSet<>());
        retainedByWindow.put("day7", new HashSet<>());
        retainedByWindow.put("day30", new HashSet<>());
        for (var event : events.findByEventTypeAndEventTimeBetweenOrderByEventTimeAsc(
                AnalyticsEvent.Authentication.Login.SUCCEEDED, from, to.plusDays(30))) {
            LocalDateTime cohort = firstRegistrationByActor.get(event.getActorId());
            if (cohort == null) continue;
            long age = ChronoUnit.DAYS.between(businessDate(cohort), businessDate(event.getEventTime()));
            if (age == 1) retainedByWindow.get("day1").add(event.getActorId());
            if (age == 7) retainedByWindow.get("day7").add(event.getActorId());
            if (age == 30) retainedByWindow.get("day30").add(event.getActorId());
        }
        Map<LocalDate, Long> cohortSizes = firstRegistrationByActor.values().stream()
                .collect(Collectors.groupingBy(this::businessDate,
                        LinkedHashMap::new, Collectors.counting()));
        return cohortSizes.entrySet().stream().map(entry -> {
            long size = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("cohortDate", entry.getKey());
            row.put("cohortSize", size);
            for (Map.Entry<String, Set<String>> window : retainedByWindow.entrySet()) {
                long retained = window.getValue().stream()
                        .filter(firstRegistrationByActor::containsKey)
                        .filter(actor -> businessDate(firstRegistrationByActor.get(actor)).equals(entry.getKey()))
                        .count();
                row.put(window.getKey() + "Retained", retained);
                row.put(window.getKey() + "Rate", size == 0 ? 0.0 : (double) retained / size);
            }
            return row;
        }).toList();
    }

    private LocalDate businessDate(LocalDateTime timestamp) {
        return timestamp.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of(properties.getBusinessTimeZone()))
                .toLocalDate();
    }

    private Map<String, PaginatedResponse<Map<String, Object>>> buildTables(AnalyticsJob job,
                                                                              Map<String, Object> summary) {
        Map<String, PaginatedResponse<Map<String, Object>>> tables = new LinkedHashMap<>();
        switch (job.getReportType()) {
            case MODERATION -> {
                addStatusTable(tables, "users", summary.get("userStatuses"), job);
                addStatusTable(tables, "formateurRequests", summary.get("formateurStatuses"), job);
                addStatusTable(tables, "reports", summary.get("reportsByStatus"), job);
            }
            case CONTENT_LEARNING -> {
                addStatusTable(tables, "feedPosts", summary.get("feedPostsByStatus"), job);
                addStatusTable(tables, "formations", summary.get("formationsByStatus"), job);
                addStatusTable(tables, "enrollments", summary.get("enrollmentsByStatus"), job);
            }
            case SUBSCRIPTIONS_PAYMENTS -> {
                addStatusTable(tables, "payments", summary.get("paymentsByStatus"), job);
                addStatusTable(tables, "subscriptions", summary.get("subscriptionsByStatus"), job);
                if (summary.get("subscriptionsBySubscriberType") instanceof Map<?, ?> byType) {
                    addStatusTable(tables, "subscriptionsArtisan", byType.get("artisan"), job);
                    addStatusTable(tables, "subscriptionsClient", byType.get("client"), job);
                }
            }
            case ENGAGEMENT, TIME_SERIES -> addStatusTable(tables, "activity", Map.of(
                    "activityEvents", summary.getOrDefault("activityEvents", 0L),
                    "messagesSent", summary.getOrDefault("messagesSent", 0L),
                    "publishedPosts", summary.getOrDefault("publishedPosts", 0L)), job);
            default -> { }
        }
        return tables;
    }

    private void addStatusTable(Map<String, PaginatedResponse<Map<String, Object>>> tables,
                                 String name, Object source, AnalyticsJob job) {
        if (!(source instanceof Map<?, ?> values)) return;
        List<Map<String, Object>> rows = values.entrySet().stream()
                .map(entry -> Map.<String, Object>of("key", String.valueOf(entry.getKey()),
                        "value", entry.getValue() == null ? 0L : entry.getValue()))
                .sorted((left, right) -> String.valueOf(left.get("key")).compareTo(String.valueOf(right.get("key"))))
                .toList();
        int pageSize = job.getPageSize();
        long requestedStart = (long) job.getPageNumber() * pageSize;
        int start = requestedStart >= rows.size() ? rows.size() : (int) requestedStart;
        int end = Math.min(start + pageSize, rows.size());
        tables.put(name, PaginatedResponse.<Map<String, Object>>builder()
                .content(rows.subList(start, end)).pageNumber(job.getPageNumber()).pageSize(pageSize)
                .totalElements(rows.size()).totalPages(rows.isEmpty() ? 0 : (rows.size() + pageSize - 1) / pageSize)
                .last(end >= rows.size()).build());
    }

    private Map<String, String> readFilters(AnalyticsJob job) {
        try {
            if (job.getFiltersJson() == null || job.getFiltersJson().isBlank()) return Map.of();
            return objectMapper.readValue(job.getFiltersJson(), new AnalyticsFiltersTypeReference());
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Analytics filters are not readable", ex);
        }
    }

    private AnalyticsEvent.Type eventTypeFilter(AnalyticsJob job) {
        String value = readFilters(job).get(AnalyticsFilterKey.EVENT_TYPE.key());
        return value == null ? null : AnalyticsEvent.fromValue(value).orElseThrow();
    }

    private long countFilteredEvents(AnalyticsJob job, LocalDateTime from, LocalDateTime to) {
        AnalyticsEvent.Type eventType = eventTypeFilter(job);
        return eventType == null ? events.countByEventTimeBetween(from, to)
                : events.countByEventTypeAndEventTimeBetween(eventType, from, to);
    }

    private long countFilteredEvent(AnalyticsJob job, AnalyticsEvent.Type eventType, LocalDateTime from, LocalDateTime to) {
        AnalyticsEvent.Type filter = eventTypeFilter(job);
        return filter != null && !filter.equals(eventType) ? 0L
                : events.countByEventTypeAndEventTimeBetween(eventType, from, to);
    }

    private long countFilteredDistinctActors(AnalyticsJob job, LocalDateTime from, LocalDateTime to) {
        AnalyticsEvent.Type eventType = eventTypeFilter(job);
        return eventType == null ? events.countDistinctActorsByEventTimeBetween(from, to)
                : events.countDistinctActorsByTypeAndEventTimeBetween(eventType, from, to);
    }

    private LocalDate firstBucketDate(LocalDate date, AnalyticsBucket bucket) {
        return switch (bucket) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.with(TemporalAdjusters.firstDayOfMonth());
            case QUARTER -> date.withMonth(((date.getMonthValue() - 1) / 3) * 3 + 1)
                    .with(TemporalAdjusters.firstDayOfMonth());
        };
    }

    private LocalDateTime utcStart(LocalDate date) {
        ZoneId businessZone = ZoneId.of(properties.getBusinessTimeZone());
        ZonedDateTime start = date.atStartOfDay(businessZone);
        return start.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private AnalyticsJobResponse response(AnalyticsJob j) { return AnalyticsJobResponse.builder().id(j.getId()).reportType(j.getReportType()).status(j.getStatus()).bucket(j.getBucket()).fromDate(j.getFromDate()).toDate(j.getToDate()).pageNumber(j.getPageNumber()).pageSize(j.getPageSize()).outputFormat(j.getOutputFormat()).completedAt(j.getCompletedAt()).expiresAt(j.getExpiresAt()).failureMessage(j.getFailureMessage()).build(); }
}
