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
import com.project.souklab.dao.analytics.AnalyticsDimensionCount;
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
import com.project.souklab.model.EnumValue;
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
                summary.put(AnalyticsMetric.Summary.TOTAL_USERS.value(), users.countByDeletedAtIsNull());
                summary.put(AnalyticsMetric.Summary.NEW_REGISTRATIONS.value(), users.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.VERIFIED_REGISTRATIONS.value(), users.countByEmailVerifiedTrueAndCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                long registrations = (long) summary.get(AnalyticsMetric.Summary.NEW_REGISTRATIONS.value());
                long verifiedRegistrations = (long) summary.get(AnalyticsMetric.Summary.VERIFIED_REGISTRATIONS.value());
                summary.put(AnalyticsMetric.Summary.ACTIVATION_RATE.value(), registrations == 0 ? 0.0 : (double) verifiedRegistrations / registrations);
                summary.put(AnalyticsMetric.Summary.VERIFIED_USERS.value(), users.countByEmailVerifiedTrueAndDeletedAtIsNull());
                summary.put(AnalyticsMetric.Summary.ARTISAN_PROFILES.value(), users.countArtisanProfiles());
                summary.put(AnalyticsMetric.Summary.CLIENT_PROFILES.value(), users.countClientProfiles());
                summary.put(AnalyticsMetric.Summary.ACTIVE_ARTISAN_PROFILES.value(), users.countActiveArtisanProfiles(AccountStatus.ACTIVE));
                summary.put(AnalyticsMetric.Summary.ACTIVE_CLIENT_PROFILES.value(), users.countActiveClientProfiles(AccountStatus.ACTIVE));
                summary.put(AnalyticsMetric.Summary.ACTIVE_USERS.value(), users.countByStatusAndDeletedAtIsNull(AccountStatus.ACTIVE));
                summary.put(AnalyticsMetric.Summary.PENDING_USERS.value(), users.countByStatusAndDeletedAtIsNull(AccountStatus.PENDING));
                summary.put(AnalyticsMetric.Summary.SUSPENDED_USERS.value(), users.countByStatusAndDeletedAtIsNull(AccountStatus.SUSPENDED));
                summary.put(AnalyticsMetric.Summary.PENDING_USER_APPROVALS.value(), users.countByStatusAndDeletedAtIsNull(AccountStatus.PENDING));
                Map<AnalyticsEvent.Type, Long> moderationActivity = new LinkedHashMap<>();
                for (AnalyticsEvent.Type eventType : List.of(AnalyticsEvent.User.APPROVED, AnalyticsEvent.User.SUSPENDED,
                        AnalyticsEvent.User.TIMED_OUT, AnalyticsEvent.User.REINSTATED,
                        AnalyticsEvent.Formation.Moderation.APPROVED, AnalyticsEvent.Formation.Moderation.REJECTED,
                        AnalyticsEvent.Report.RESOLVED)) {
                    moderationActivity.put(eventType, countFilteredEvent(job, eventType, from, inclusiveTo));
                }
                summary.put(AnalyticsMetric.Summary.MODERATION_ACTIVITY.value(), moderationActivity);
                Map<AccountStatus, Long> userStatuses = new LinkedHashMap<>();
                for (AccountStatus status : AccountStatus.values()) {
                    userStatuses.put(status, users.countByStatusAndDeletedAtIsNull(status));
                }
                summary.put(AnalyticsMetric.Summary.USER_STATUSES.value(), userStatuses);
                summary.put(AnalyticsMetric.Summary.ACTIVITY_EVENTS.value(), countFilteredEvents(job, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.SUCCESSFUL_LOGINS.value(), countFilteredEvent(job, AnalyticsEvent.Authentication.Login.SUCCEEDED, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.PUBLISHED_POSTS.value(), countFilteredEvent(job, AnalyticsEvent.Feed.Post.PUBLISHED, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.MESSAGES_SENT.value(), countFilteredEvent(job, AnalyticsEvent.Message.SENT, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.PROFILE_VIEWS.value(), countFilteredEvent(job, AnalyticsEvent.Profile.VIEW, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.REPORT_RESOLUTIONS.value(), countFilteredEvent(job, AnalyticsEvent.Report.RESOLVED, from, inclusiveTo));
                LocalDateTime activityDayStart = utcStart(job.getToDate());
                LocalDateTime activityWeekStart = utcStart(job.getToDate().minusDays(6));
                LocalDateTime activityMonthStart = utcStart(job.getToDate().minusDays(29));
                summary.put(AnalyticsMetric.Summary.DAU.value(), countFilteredDistinctActors(job, activityDayStart, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.WAU.value(), countFilteredDistinctActors(job, activityWeekStart, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.MAU.value(), countFilteredDistinctActors(job, activityMonthStart, inclusiveTo));
                AnalyticsEvent.Type eventFilter = eventTypeFilter(job);
                summary.put(AnalyticsMetric.Summary.ENGAGEMENT_BY_ACCOUNT_TYPE.value(), Map.of(
                        AnalyticsMetric.AccountType.ARTISAN.value(), eventFilter == null
                                ? events.countDistinctArtisanActorsByEventTimeBetween(from, inclusiveTo)
                                : events.countDistinctArtisanActorsByTypeAndEventTimeBetween(eventFilter, from, inclusiveTo),
                        AnalyticsMetric.AccountType.CLIENT.value(), eventFilter == null
                                ? events.countDistinctClientActorsByEventTimeBetween(from, inclusiveTo)
                                : events.countDistinctClientActorsByTypeAndEventTimeBetween(eventFilter, from, inclusiveTo)));
                summary.put(AnalyticsMetric.Summary.ENGAGEMENT_BY_REGION.value(), dimensionCounts(
                        events.countDistinctActorsByRegionAndEventTimeBetween(eventFilter, from, inclusiveTo)));
                summary.put(AnalyticsMetric.Summary.ENGAGEMENT_BY_CRAFT_CATEGORY.value(), dimensionCounts(
                        events.countDistinctActorsByCraftCategoryAndEventTimeBetween(eventFilter, from, inclusiveTo)));
                if (job.getReportType() == AnalyticsReportType.GROWTH) {
                    summary.put(AnalyticsMetric.Summary.LOGIN_RETENTION_COHORTS.value(), loginRetentionCohorts(from, inclusiveTo));
                }
                if (feedPosts != null) summary.put(AnalyticsMetric.Summary.FEED_POSTS_CREATED.value(), feedPosts.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (formations != null) {
                    summary.put(AnalyticsMetric.Summary.FORMATIONS_CREATED.value(), formations.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                    summary.put(AnalyticsMetric.Summary.ACTIVE_INSTRUCTORS.value(),
                            formations.countDistinctAuthorsByStatusAndDeletedAtIsNull(FormationStatus.PUBLISHED));
                }
                if (enrollments != null) summary.put(AnalyticsMetric.Summary.FORMATION_ENROLLMENTS.value(), enrollments.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (reviews != null) summary.put(AnalyticsMetric.Summary.REVIEWS_SUBMITTED.value(), reviews.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (reviews != null) {
                    summary.put(AnalyticsMetric.Summary.PUBLISHED_REVIEWS.value(), reviews.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                            ReviewStatus.PUBLISHED, from, inclusiveTo));
                    BigDecimal averageRating = reviews.averageRatingByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                            ReviewStatus.PUBLISHED, from, inclusiveTo);
                    summary.put(AnalyticsMetric.Summary.AVERAGE_PUBLISHED_RATING.value(), averageRating == null ? BigDecimal.ZERO : averageRating);
                }
                if (reports != null) summary.put(AnalyticsMetric.Summary.REPORTS_SUBMITTED.value(), reports.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (reports != null) {
                    Double averageResolutionSeconds = reports.averageResolutionSecondsByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo);
                    summary.put(AnalyticsMetric.Summary.AVERAGE_REPORT_RESOLUTION_SECONDS.value(),
                            averageResolutionSeconds == null ? 0.0 : averageResolutionSeconds);
                }
                summary.put(AnalyticsMetric.Summary.FORMATEUR_PENDING.value(), formateurRequests.countByStatusAndDeletedAtIsNull(FormateurRequestStatus.PENDING));
                summary.put(AnalyticsMetric.Summary.FORMATEUR_APPROVED_IN_RANGE.value(), formateurRequests.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                        FormateurRequestStatus.APPROVED, from, inclusiveTo));
                summary.put(AnalyticsMetric.Summary.FORMATEUR_REJECTED_IN_RANGE.value(), formateurRequests.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                        FormateurRequestStatus.REJECTED, from, inclusiveTo));
                Map<FormateurRequestStatus, Long> formateurStatuses = new LinkedHashMap<>();
                for (FormateurRequestStatus status : FormateurRequestStatus.values()) {
                    formateurStatuses.put(status, formateurRequests.countByStatusAndDeletedAtIsNull(status));
                }
                summary.put(AnalyticsMetric.Summary.FORMATEUR_STATUSES.value(), formateurStatuses);
                if (payments != null) summary.put(AnalyticsMetric.Summary.PAYMENTS_CREATED.value(), payments.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo));
                if (feedPosts != null) {
                    Map<FeedPostStatus, Long> statuses = new LinkedHashMap<>();
                    for (FeedPostStatus status : FeedPostStatus.values()) statuses.put(status, feedPosts.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put(AnalyticsMetric.Summary.FEED_POSTS_BY_STATUS.value(), statuses);
                }
                if (formations != null) {
                    Map<FormationStatus, Long> statuses = new LinkedHashMap<>();
                    for (FormationStatus status : FormationStatus.values()) statuses.put(status, formations.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put(AnalyticsMetric.Summary.FORMATIONS_BY_STATUS.value(), statuses);
                }
                if (enrollments != null) {
                    Map<EnrollmentStatus, Long> statuses = new LinkedHashMap<>();
                    for (EnrollmentStatus status : EnrollmentStatus.values()) statuses.put(status, enrollments.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put(AnalyticsMetric.Summary.ENROLLMENTS_BY_STATUS.value(), statuses);
                    long enrollmentTotal = statuses.values().stream().mapToLong(Long::longValue).sum();
                    summary.put(AnalyticsMetric.Summary.FORMATION_COMPLETIONS.value(), statuses.getOrDefault(EnrollmentStatus.ATTENDED, 0L));
                    summary.put(AnalyticsMetric.Summary.ENROLLMENT_CANCELLATION_RATE.value(), enrollmentTotal == 0 ? 0.0
                            : (double) statuses.getOrDefault(EnrollmentStatus.CANCELLED, 0L) / enrollmentTotal);
                    if (formations != null) {
                        long capacity = formations.sumMaxParticipantsByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                                FormationStatus.PUBLISHED, from, inclusiveTo);
                        long confirmed = enrollments.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
                                EnrollmentStatus.CONFIRMED, from, inclusiveTo);
                        summary.put(AnalyticsMetric.Summary.FORMATION_UTILIZATION_RATE.value(),
                                capacity == 0 ? 0.0 : (double) confirmed / capacity);
                    }
                }
                if (reports != null) {
                    Map<ReportStatus, Long> statuses = new LinkedHashMap<>();
                    for (ReportStatus status : ReportStatus.values()) statuses.put(status, reports.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    summary.put(AnalyticsMetric.Summary.REPORTS_BY_STATUS.value(), statuses);
                }
                if (payments != null) {
                    Map<PaymentStatus, Long> statuses = new LinkedHashMap<>();
                        for (PaymentStatus status : PaymentStatus.values()) {
                        statuses.put(status, payments.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                    }
                    summary.put(AnalyticsMetric.Summary.PAYMENTS_BY_STATUS.value(), statuses);
                    if (job.getReportType() == AnalyticsReportType.SUBSCRIPTIONS_PAYMENTS) {
                        Map<SubscriptionStatus, Long> subscriptions = new LinkedHashMap<>();
                        for (SubscriptionStatus status : SubscriptionStatus.values()) {
                            subscriptions.put(status, artisanSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo)
                                    + clientSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                        }
                        summary.put(AnalyticsMetric.Summary.SUBSCRIPTIONS_BY_STATUS.value(), subscriptions);
                        Map<String, Map<?, Long>> subscriptionsBySubscriberType = new LinkedHashMap<>();
                        Map<SubscriptionStatus, Long> artisanSubscriptionStatuses = new LinkedHashMap<>();
                        Map<SubscriptionStatus, Long> clientSubscriptionStatuses = new LinkedHashMap<>();
                            for (SubscriptionStatus status : SubscriptionStatus.values()) {
                            artisanSubscriptionStatuses.put(status, artisanSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                            clientSubscriptionStatuses.put(status, clientSubscriptions.countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(status, from, inclusiveTo));
                        }
                        subscriptionsBySubscriberType.put(AnalyticsMetric.AccountType.ARTISAN.value(), artisanSubscriptionStatuses);
                        subscriptionsBySubscriberType.put(AnalyticsMetric.AccountType.CLIENT.value(), clientSubscriptionStatuses);
                        summary.put(AnalyticsMetric.Summary.SUBSCRIPTIONS_BY_SUBSCRIBER_TYPE.value(), subscriptionsBySubscriberType);
                        Map<AnalyticsEvent.Type, Long> lifecycleEvents = new LinkedHashMap<>();
                        for (AnalyticsEvent.Type eventType : List.of(AnalyticsEvent.Subscription.ACTIVATED, AnalyticsEvent.Subscription.EXPIRED,
                                AnalyticsEvent.Subscription.CANCELED, AnalyticsEvent.Subscription.REVOKED, AnalyticsEvent.Subscription.RENEWAL)) {
                            lifecycleEvents.put(eventType, countFilteredEvent(job, eventType, from, inclusiveTo));
                        }
                        summary.put(AnalyticsMetric.Summary.SUBSCRIPTION_LIFECYCLE_EVENTS.value(), lifecycleEvents);
                        summary.put(AnalyticsMetric.Summary.CHECKOUT_CREATED.value(), countFilteredEvent(job, AnalyticsEvent.Checkout.CREATED, from, inclusiveTo));
                        summary.put(AnalyticsMetric.Summary.PAYMENT_STATE_TRANSITIONS.value(), countFilteredEvent(job, AnalyticsEvent.Payment.State.TRANSITION, from, inclusiveTo));
                        String collectedCurrency = appProperties.getSubscription().getCurrency();
                        long grossCollected = payments.sumAmountByStatusAndCurrencyAndCreatedAtBetween(
                                PaymentStatus.PAID, collectedCurrency, from, to);
                        long providerFees = payments.sumFeesByStatusAndCurrencyAndCreatedAtBetween(
                                PaymentStatus.PAID, collectedCurrency, from, to);
                        summary.put(AnalyticsMetric.Summary.GROSS_COLLECTED_DZD.value(), grossCollected);
                        summary.put(AnalyticsMetric.Summary.PROVIDER_FEES_DZD.value(), providerFees);
                        summary.put(AnalyticsMetric.Summary.NET_COLLECTED_DZD.value(), grossCollected - providerFees);
                        long paidPayments = payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.PAID, from, inclusiveTo);
                        long failedPayments = payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.FAILED, from, inclusiveTo)
                                + payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.CANCELED, from, inclusiveTo)
                                + payments.countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(
                                PaymentStatus.EXPIRED, from, inclusiveTo);
                        summary.put(AnalyticsMetric.Summary.PAYMENT_CONVERSION_RATE.value(), paidPayments + failedPayments == 0 ? 0.0
                                : (double) paidPayments / (paidPayments + failedPayments));
                        summary.put(AnalyticsMetric.Summary.MANUAL_GRANTS.value(), payments.countByManualGrantTrueAndCreatedAtBetweenAndDeletedAtIsNull(
                                from, inclusiveTo));
                    }
                }
                if (job.getReportType() == AnalyticsReportType.OPERATIONAL) {
                    Map<AnalyticsMetric.Key, Object> operational = new LinkedHashMap<>();
                    operational.put(AnalyticsMetric.Operational.ANALYTICS_JOBS_QUEUED, jobs.countByStatus(AnalyticsJobStatus.QUEUED));
                    operational.put(AnalyticsMetric.Operational.ANALYTICS_JOBS_RUNNING, jobs.countByStatus(AnalyticsJobStatus.RUNNING));
                    operational.put(AnalyticsMetric.Operational.ANALYTICS_JOBS_COMPLETED, jobs.countByStatus(AnalyticsJobStatus.COMPLETED));
                    operational.put(AnalyticsMetric.Operational.ANALYTICS_JOBS_FAILED, jobs.countByStatus(AnalyticsJobStatus.FAILED));
                    if (maintenanceJobs != null) {
                        operational.put(AnalyticsMetric.Operational.MAINTENANCE_JOBS_QUEUED, maintenanceJobs.countByStatus(AnalyticsJobStatus.QUEUED));
                        operational.put(AnalyticsMetric.Operational.MAINTENANCE_JOBS_RUNNING, maintenanceJobs.countByStatus(AnalyticsJobStatus.RUNNING));
                        operational.put(AnalyticsMetric.Operational.MAINTENANCE_JOBS_COMPLETED, maintenanceJobs.countByStatus(AnalyticsJobStatus.COMPLETED));
                        operational.put(AnalyticsMetric.Operational.MAINTENANCE_JOBS_FAILED, maintenanceJobs.countByStatus(AnalyticsJobStatus.FAILED));
                    }
                    operational.put(AnalyticsMetric.Operational.OUTBOX_PENDING, outbox.countByStatus(OutboxStatus.PENDING));
                    operational.put(AnalyticsMetric.Operational.OUTBOX_PUBLISHED, outbox.countByStatus(OutboxStatus.PUBLISHED));
                    operational.put(AnalyticsMetric.Operational.OUTBOX_DEAD_LETTER, outbox.countByStatus(OutboxStatus.DEAD_LETTER));
                    if (healthEndpoint != null) {
                        HealthDescriptor health = healthEndpoint.health();
                        operational.put(AnalyticsMetric.Operational.APPLICATION_HEALTH, health.getStatus().getCode());
                        operational.put(AnalyticsMetric.Operational.HEALTH_COMPONENTS, healthComponentStatuses(health));
                    }
                    if (operationalMetrics != null) {
                        operational.put(AnalyticsMetric.Operational.REQUEST_COUNTERS, operationalMetrics.snapshot(AnalyticsMetric.Operational.REQUEST_COUNTER_METRIC.value()));
                        operational.put(AnalyticsMetric.Operational.RATE_LIMIT_REJECTIONS, operationalMetrics.snapshot(AnalyticsMetric.Operational.RATE_LIMIT_REJECTION_METRIC.value()));
                    }
                    summary.put(AnalyticsMetric.Summary.OPERATIONAL.value(), operational);
                }
                long rangeDays = ChronoUnit.DAYS.between(job.getFromDate(), job.getToDate()) + 1;
                LocalDateTime previousFrom = from.minusDays(rangeDays);
                LocalDateTime previousTo = from.minusNanos(1);
                Map<AnalyticsMetric.Key, Object> comparison = new LinkedHashMap<>();
                comparison.put(AnalyticsMetric.Summary.NEW_REGISTRATIONS, Map.of(
                        AnalyticsMetric.Comparison.CURRENT, users.countByCreatedAtBetweenAndDeletedAtIsNull(from, inclusiveTo),
                        AnalyticsMetric.Comparison.PREVIOUS, users.countByCreatedAtBetweenAndDeletedAtIsNull(previousFrom, previousTo)));
                comparison.put(AnalyticsMetric.Summary.ACTIVITY_EVENTS, Map.of(
                        AnalyticsMetric.Comparison.CURRENT, countFilteredEvents(job, from, inclusiveTo),
                        AnalyticsMetric.Comparison.PREVIOUS, countFilteredEvents(job, previousFrom, previousTo)));
                summary.put(AnalyticsMetric.Summary.PERIOD_COMPARISON.value(), comparison);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put(AnalyticsMetric.Result.REPORT_TYPE.value(), job.getReportType());
                result.put(AnalyticsMetric.Result.FROM_DATE.value(), job.getFromDate());
                result.put(AnalyticsMetric.Result.TO_DATE.value(), job.getToDate());
                result.put(AnalyticsMetric.Result.BUCKET.value(), job.getBucket());
                result.put(AnalyticsMetric.Result.SUMMARY.value(), summary);
                result.put(AnalyticsMetric.Result.TABLES.value(), buildTables(job, summary));
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
                result.put(AnalyticsMetric.Result.SERIES.value(), PaginatedResponse.<Map<String, Object>>builder()
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
        AnalyticsJob job = ownerJob(id, username);
        audit(AuditLogAction.ANALYTICS_RESULT_READ, job, "outcome=STATUS");
        return response(job);
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
            var summary = root.path(AnalyticsMetric.Result.SUMMARY.value());
            var series = root.path(AnalyticsMetric.Result.SERIES.value()).path(AnalyticsMetric.Result.CONTENT.value());
            StringBuilder csv = new StringBuilder("section,key,value\n");
            summary.fields().forEachRemaining(e -> csv.append(csvCell(AnalyticsMetric.Result.SUMMARY.value())).append(',')
                    .append(csvCell(e.getKey())).append(',')
                    .append(csvCell(e.getValue().isContainerNode() ? e.getValue().toString() : e.getValue().asText()))
                    .append('\n'));
            for (int index = 0; index < series.size(); index++) {
                final int pointIndex = index;
                var point = series.get(index);
                point.fields().forEachRemaining(e -> csv.append(csvCell(AnalyticsMetric.Csv.SERIES_PREFIX.value() + pointIndex + "]")).append(',')
                        .append(csvCell(e.getKey())).append(',')
                        .append(csvCell(e.getValue().isContainerNode() ? e.getValue().toString() : e.getValue().asText()))
                        .append('\n'));
            }
            var tables = root.path(AnalyticsMetric.Result.TABLES.value());
            tables.fields().forEachRemaining(table -> {
                var content = table.getValue().path(AnalyticsMetric.Result.CONTENT.value());
                for (int index = 0; index < content.size(); index++) {
                    var row = content.get(index);
                    row.fields().forEachRemaining(entry -> csv.append(csvCell(AnalyticsMetric.Csv.TABLE_PREFIX.value() + table.getKey()))
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
        if (properties.getSupportedBuckets() != null && !properties.getSupportedBuckets().isEmpty()
                && !properties.getSupportedBuckets().contains(r.getBucket())) {
            throw new BadRequestException("Analytics bucket is not enabled by configuration");
        }
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
        if (r.getFilters() != null && r.getFilters().containsKey(AnalyticsFilterKey.EVENT_TYPE)
                && AnalyticsEvent.fromValue(r.getFilters().get(AnalyticsFilterKey.EVENT_TYPE)).isEmpty()) {
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
            point.put(AnalyticsMetric.Series.START_DATE.value(), start);
            point.put(AnalyticsMetric.Series.END_DATE.value(), end);
            AnalyticsEvent.Type eventType = AnalyticsEvent.fromValue(filters.get(AnalyticsFilterKey.EVENT_TYPE.key())).orElse(null);
            LocalDateTime inclusiveBucketTo = bucketTo.minusNanos(1);
            point.put(AnalyticsMetric.Series.ACTIVITY_EVENTS.value(), eventType == null
                    ? events.countByEventTimeBetween(bucketFrom, inclusiveBucketTo)
                    : events.countByEventTypeAndEventTimeBetween(eventType, bucketFrom, inclusiveBucketTo));
            point.put(AnalyticsMetric.Series.UNIQUE_ACTORS.value(), eventType == null
                    ? events.countDistinctActorsByEventTimeBetween(bucketFrom, inclusiveBucketTo)
                    : events.countDistinctActorsByTypeAndEventTimeBetween(eventType, bucketFrom, inclusiveBucketTo));
            point.put(AnalyticsMetric.Series.NEW_REGISTRATIONS.value(), users.countByCreatedAtBetweenAndDeletedAtIsNull(bucketFrom, inclusiveBucketTo));
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
        Map<AnalyticsMetric.Retention, Set<String>> retainedByWindow = new LinkedHashMap<>();
        retainedByWindow.put(AnalyticsMetric.Retention.DAY_1, new HashSet<>());
        retainedByWindow.put(AnalyticsMetric.Retention.DAY_7, new HashSet<>());
        retainedByWindow.put(AnalyticsMetric.Retention.DAY_30, new HashSet<>());
        for (var event : events.findByEventTypeAndEventTimeBetweenOrderByEventTimeAsc(
                AnalyticsEvent.Authentication.Login.SUCCEEDED, from, to.plusDays(30))) {
            LocalDateTime cohort = firstRegistrationByActor.get(event.getActorId());
            if (cohort == null) continue;
            long age = ChronoUnit.DAYS.between(businessDate(cohort), businessDate(event.getEventTime()));
            if (age == 1) retainedByWindow.get(AnalyticsMetric.Retention.DAY_1).add(event.getActorId());
            if (age == 7) retainedByWindow.get(AnalyticsMetric.Retention.DAY_7).add(event.getActorId());
            if (age == 30) retainedByWindow.get(AnalyticsMetric.Retention.DAY_30).add(event.getActorId());
        }
        Map<LocalDate, Long> cohortSizes = firstRegistrationByActor.values().stream()
                .collect(Collectors.groupingBy(this::businessDate,
                        LinkedHashMap::new, Collectors.counting()));
        return cohortSizes.entrySet().stream().map(entry -> {
            long size = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(AnalyticsMetric.Retention.COHORT_DATE.value(), entry.getKey());
            row.put(AnalyticsMetric.Retention.COHORT_SIZE.value(), size);
            for (Map.Entry<AnalyticsMetric.Retention, Set<String>> window : retainedByWindow.entrySet()) {
                long retained = window.getValue().stream()
                        .filter(firstRegistrationByActor::containsKey)
                        .filter(actor -> businessDate(firstRegistrationByActor.get(actor)).equals(entry.getKey()))
                        .count();
                row.put(window.getKey().value() + AnalyticsMetric.Retention.RETAINED_SUFFIX.value(), retained);
                row.put(window.getKey().value() + AnalyticsMetric.Retention.RATE_SUFFIX.value(), size == 0 ? 0.0 : (double) retained / size);
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
                addStatusTable(tables, AnalyticsMetric.Table.USERS, summary.get(AnalyticsMetric.Summary.USER_STATUSES.value()), job);
                addStatusTable(tables, AnalyticsMetric.Table.FORMATEUR_REQUESTS, summary.get(AnalyticsMetric.Summary.FORMATEUR_STATUSES.value()), job);
                addStatusTable(tables, AnalyticsMetric.Table.REPORTS, summary.get(AnalyticsMetric.Summary.REPORTS_BY_STATUS.value()), job);
            }
            case CONTENT_LEARNING -> {
                addStatusTable(tables, AnalyticsMetric.Table.FEED_POSTS, summary.get(AnalyticsMetric.Summary.FEED_POSTS_BY_STATUS.value()), job);
                addStatusTable(tables, AnalyticsMetric.Table.FORMATIONS, summary.get(AnalyticsMetric.Summary.FORMATIONS_BY_STATUS.value()), job);
                addStatusTable(tables, AnalyticsMetric.Table.ENROLLMENTS, summary.get(AnalyticsMetric.Summary.ENROLLMENTS_BY_STATUS.value()), job);
            }
            case SUBSCRIPTIONS_PAYMENTS -> {
                addStatusTable(tables, AnalyticsMetric.Table.PAYMENTS, summary.get(AnalyticsMetric.Summary.PAYMENTS_BY_STATUS.value()), job);
                addStatusTable(tables, AnalyticsMetric.Table.SUBSCRIPTIONS, summary.get(AnalyticsMetric.Summary.SUBSCRIPTIONS_BY_STATUS.value()), job);
                if (summary.get(AnalyticsMetric.Summary.SUBSCRIPTIONS_BY_SUBSCRIBER_TYPE.value()) instanceof Map<?, ?> byType) {
                    addStatusTable(tables, AnalyticsMetric.Table.SUBSCRIPTIONS_ARTISAN, byType.get(AnalyticsMetric.AccountType.ARTISAN.value()), job);
                    addStatusTable(tables, AnalyticsMetric.Table.SUBSCRIPTIONS_CLIENT, byType.get(AnalyticsMetric.AccountType.CLIENT.value()), job);
                }
            }
            case ENGAGEMENT, TIME_SERIES -> addStatusTable(tables, AnalyticsMetric.Table.ACTIVITY, Map.of(
                    AnalyticsMetric.Summary.ACTIVITY_EVENTS.value(), summary.getOrDefault(AnalyticsMetric.Summary.ACTIVITY_EVENTS.value(), 0L),
                    AnalyticsMetric.Summary.MESSAGES_SENT.value(), summary.getOrDefault(AnalyticsMetric.Summary.MESSAGES_SENT.value(), 0L),
                    AnalyticsMetric.Summary.PUBLISHED_POSTS.value(), summary.getOrDefault(AnalyticsMetric.Summary.PUBLISHED_POSTS.value(), 0L),
                    AnalyticsMetric.Summary.PROFILE_VIEWS.value(), summary.getOrDefault(AnalyticsMetric.Summary.PROFILE_VIEWS.value(), 0L),
                    AnalyticsMetric.Summary.REPORT_RESOLUTIONS.value(), summary.getOrDefault(AnalyticsMetric.Summary.REPORT_RESOLUTIONS.value(), 0L)), job);
            default -> { }
        }
        return tables;
    }

    private void addStatusTable(Map<String, PaginatedResponse<Map<String, Object>>> tables,
                                 AnalyticsMetric.Table table, Object source, AnalyticsJob job) {
        if (!(source instanceof Map<?, ?> values)) return;
        List<Map<String, Object>> rows = values.entrySet().stream()
                .map(entry -> Map.<String, Object>of(AnalyticsMetric.Csv.KEY.value(), keyValue(entry.getKey()),
                        AnalyticsMetric.Csv.VALUE.value(), entry.getValue() == null ? 0L : entry.getValue()))
                .sorted((left, right) -> String.valueOf(left.get(AnalyticsMetric.Csv.KEY.value())).compareTo(String.valueOf(right.get(AnalyticsMetric.Csv.KEY.value()))))
                .toList();
        int pageSize = job.getPageSize();
        long requestedStart = (long) job.getPageNumber() * pageSize;
        int start = requestedStart >= rows.size() ? rows.size() : (int) requestedStart;
        int end = Math.min(start + pageSize, rows.size());
        tables.put(table.value(), PaginatedResponse.<Map<String, Object>>builder()
                .content(rows.subList(start, end)).pageNumber(job.getPageNumber()).pageSize(pageSize)
                .totalElements(rows.size()).totalPages(rows.isEmpty() ? 0 : (rows.size() + pageSize - 1) / pageSize)
                .last(end >= rows.size()).build());
    }

    private String keyValue(Object key) {
        if (key instanceof AnalyticsEvent.Type eventType) return eventType.value();
        if (key instanceof EnumValue enumValue) return enumValue.value();
        if (key instanceof AnalyticsMetric.Key metricKey) return metricKey.value();
        return String.valueOf(key);
    }

    private Map<String, Long> dimensionCounts(List<AnalyticsDimensionCount> counts) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (AnalyticsDimensionCount count : counts) {
            result.put(count.getDimension(), count.getCount());
        }
        return result;
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
