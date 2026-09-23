# Souklab Backend Implementation History

## Document purpose

This is a source-based handoff of the backend implementation completed across
Phases 1 through 10. It is written for a future engineer who needs to
understand what exists before changing or verifying it. The code snapshots
below are shortened excerpts from the current repository as of 2026-09-20;
the file path above each snapshot is the authoritative source.

The snapshots are intentionally small. They show the actual architectural
contracts and representative behavior, while the complete implementation
remains in the linked Java files.

## Technology and cross-cutting conventions

- Java 21 and Spring Boot 4.x.
- MariaDB/JPA for transactional persistence and Flyway migrations.
- Spring Security with stateless JWT authentication and method authorization.
- Redis/Bucket4j-capable rate limiting.
- Elasticsearch 8/Hibernate Search for public directory search.
- S3/MinIO-compatible object storage with ClamAV scanning.
- RabbitMQ STOMP relay for realtime messaging and RabbitMQ analytics delivery.
- Jackson DTOs use the common `ApiResponse<T>` and
  `PaginatedResponse<T>` contracts.
- A configured `Clock` is used for time-sensitive application behavior.
- Application policy is externalized through typed configuration classes.
- Event, permission, audit, metric, filter, and notification taxonomies use
  nested enums where compound names are meaningful; stable wire/database
  strings are retained only at serialization or persistence boundaries.

## Phase 1 — Build infrastructure and core foundation

### Delivered capabilities

- Maven/Spring Boot project baseline and Java 21 toolchain.
- Typed application configuration and environment-backed policy.
- Shared entity identity, timestamps, soft deletion, response envelopes, and
  pagination contracts.
- Central exception handling and validation response shape.

### Source snapshots

`src/main/java/com/project/souklab/model/BaseEntity.java`

```java
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void ensureId() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
```

`src/main/java/com/project/souklab/dto/common/ApiResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String errorCode;
    private String message;
    private T data;
    private Map<String, String> errors;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }
}
```

## Phase 2 — Security, authentication, and identity

### Delivered capabilities

- User, permission, refresh-token, and verification-token persistence.
- Registration, login, refresh, logout, email verification, password reset,
  password change, OAuth entry points, current-user profile, and completion
  flows.
- JWT validation with injected time, stateless Spring Security, CORS policy,
  method security, standardized unauthorized/forbidden responses, and rate
  limiting.
- Account enabled/locked checks before establishing a security context.

### Source snapshots

`src/main/java/com/project/souklab/config/SecurityConfig.java`

```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/actuator/health/liveness",
                    "/actuator/health/readiness",
                    "/api/v1/catalog/**",
                    "/api/v1/public/**")
            .permitAll()
            .anyRequest().authenticated());

http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

`src/main/java/com/project/souklab/security/JwtAuthenticationFilter.java`

```java
String jwt = parseJwt(request);
if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
    String username = jwtUtils.getUserNameFromJwtToken(jwt);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
        filterChain.doFilter(request, response);
        return;
    }
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

## Phase D — File storage, antivirus, and avatar pipeline

This file-storage phase was implemented before the numbered catalog phases.

### Delivered capabilities

- Provider-neutral storage interface with in-memory and S3/MinIO providers.
- ClamAV `zINSTREAM` scanning with configured timeout and fail-open/fail-closed
  policy.
- Thumbnail, medium, and original image variants with no upscaling.
- Avatar entity/gallery management, quota enforcement, activation, deletion,
  streaming file access, immutable caching, upload-size limits, and dedicated
  rate limiting.

### Source snapshots

`src/main/java/com/project/souklab/filestorage/StorageService.java`

```java
public interface StorageService {
    StorageResult store(InputStream content, String originalFilename,
                        String contentType, long size);
    StorageResource retrieve(String key);
    default StorageResource load(String key) {
        return retrieve(key);
    }
    void delete(String key);
    boolean exists(String key);
}
```

`src/main/java/com/project/souklab/filestorage/image/ResolutionTier.java`

```java
public enum ResolutionTier implements EnumValue {
    THUMBNAIL(THUMBNAIL_MAX_DIMENSION),
    MEDIUM(MEDIUM_MAX_DIMENSION),
    ORIGINAL(ORIGINAL_MAX_DIMENSION);

    public static final int THUMBNAIL_MAX_DIMENSION = 150;
    public static final int MEDIUM_MAX_DIMENSION = 500;
    public static final int ORIGINAL_MAX_DIMENSION = 2000;

    private final int maxDimension;
}
```

`src/main/java/com/project/souklab/filestorage/scan/ClamdInstreamScanner.java`

```java
out.write(INSTREAM_COMMAND);
out.flush();
while ((bytesRead = content.read(buffer)) != -1) {
    if (bytesRead > 0) {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4).putInt(bytesRead);
        out.write(lengthBuffer.array());
        out.write(buffer, 0, bytesRead);
    }
}
out.write(TERMINATION_CHUNK);
out.flush();
return parseResponse(readResponse(in));
```

## Phase 3 — Catalog, taxonomy, and heritage reference data

### Delivered capabilities

- Hierarchical regions (wilayas/communes), job categories/subcategories,
  material families/materials, historical epochs, and techniques.
- Unique slugs, display ordering, repositories, cache-backed read paths, and
  seeded Algerian reference data.
- Public catalog endpoints for each taxonomy family.

### Source snapshot

`src/main/java/com/project/souklab/controller/catalog/CatalogController.java`

```java
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionDTO>>> getRegions() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllRegions()));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<JobCategoryDTO>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllCategories()));
    }
}
```

## Phase 4 — Artisan and client profiles

### Delivered capabilities

- Artisan and client aggregates linked to users.
- Multi-step profile completion for both account types.
- Profile views with deduplication and premium-gated contact masking.
- Artisan gallery images, certifications, achievements, social links, and
  taxonomy join tables.
- Portfolio and certification management endpoints.

### Representative source locations

- `src/main/java/com/project/souklab/model/Artisan.java`
- `src/main/java/com/project/souklab/model/Client.java`
- `src/main/java/com/project/souklab/service/profile/ProfileService.java`
- `src/main/java/com/project/souklab/service/artisan/ArtisanService.java`
- `src/main/java/com/project/souklab/dao/ArtisanProfileViewRepository.java`
- `src/main/java/com/project/souklab/controller/artisan/ArtisanController.java`
- `src/main/java/com/project/souklab/controller/artisan/ArtisanGalleryController.java`

The profile and directory response contracts use DTOs rather than exposing
JPA entities directly, allowing contact information to be masked at the
service boundary.

## Phase 5 — Elasticsearch directory search

### Delivered capabilities

- Hibernate Search indexing on the artisan aggregate and taxonomy fields.
- Edge n-gram, ASCII-folding, name, keyword, region, craft, material, epoch,
  and technique search support.
- Faceted filtering, pagination, adaptive fuzziness, and relational fallback.
- Asynchronous index initialization and integration/Postman coverage.

### Source snapshot

`src/main/java/com/project/souklab/controller/directory/DirectoryController.java`

```java
@RestController
@RequestMapping("/api/v1/public/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectorySearchService directorySearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ArtisanDirectoryCardDTO>>> search(
            @Valid @ModelAttribute DirectorySearchFilterDTO filter) {
        PaginatedResponse<ArtisanDirectoryCardDTO> response =
                directorySearchService.search(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

## Phase 6 — Formations, workshops, and formateur governance

### Delivered capabilities

- Formateur application, approval/rejection, cooldown, permanent-block, grant,
  revoke, and lift-cooldown workflows.
- Formation authoring, draft/submission/review/publish lifecycle, thumbnails,
  protected files, and compensating storage rollback.
- Capacity-bounded enrollment, cancellation cutoff, enrollment history,
  reviews, and protected course downloads.
- Admin moderation and notifications.

### Representative source locations

- `src/main/java/com/project/souklab/model/Formation.java`
- `src/main/java/com/project/souklab/model/FormationEnrollment.java`
- `src/main/java/com/project/souklab/model/ArtisanFormateurRequest.java`
- `src/main/java/com/project/souklab/service/formation/FormationService.java`
- `src/main/java/com/project/souklab/service/formateur/ArtisanFormateurService.java`
- `src/main/java/com/project/souklab/controller/formation/`
- `src/main/java/com/project/souklab/controller/formateur/`

The Phase 6 behavior is covered by `FormationIntegrationTest` and the Batch 8
Postman scenarios documented in `docs/dev/POSTMAN_API_REFERENCE.md`.

## Phase 7 — Feed, reviews, reports, and moderation

### Delivered capabilities

- Moderated feed posts for news, formation, and announcement types.
- Moderated multi-image post attachments.
- Decimal artisan reviews tied to attended formations and aggregate-rating
  recalculation.
- Content reports for users, posts, and reviews, with administrator resolution
  actions and audit behavior.

### Representative source locations

- `src/main/java/com/project/souklab/model/FeedPost.java`
- `src/main/java/com/project/souklab/model/ArtisanReview.java`
- `src/main/java/com/project/souklab/model/ContentReport.java`
- `src/main/java/com/project/souklab/controller/feed/FeedPostController.java`
- `src/main/java/com/project/souklab/controller/report/ContentReportController.java`
- `src/main/java/com/project/souklab/controller/review/ArtisanReviewController.java`

## Phase 8 — Realtime messaging and notifications

### Delivered capabilities

- WebSocket/STOMP endpoints and authenticated inbound channel interceptor.
- RabbitMQ external broker relay and user-destination routing.
- Private conversations, participants, messages, attachments, REST history,
  idempotent sending, edits, soft deletes, read-up-to receipts, typing, and
  presence events.
- Persisted notifications, unread-count semantics, soft-delete exclusion,
  query-scoped user isolation, and after-commit realtime delivery.

### Source snapshot

`src/main/java/com/project/souklab/config/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final AppProperties appProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        AppProperties.Relay relay = appProperties.getRelay();
        registry.enableStompBrokerRelay(
                appProperties.getChat().getBrokerDestinationPrefixes().split(","))
                .setRelayHost(relay.getHost())
                .setRelayPort(relay.getPort())
                .setClientLogin(relay.getClientLogin())
                .setClientPasscode(relay.getClientPasscode());
        registry.setApplicationDestinationPrefixes(
                appProperties.getChat().getApplicationDestinationPrefix());
    }
}
```

## Phase 9 — Subscriptions, payments, and Chargily

### Delivered capabilities

- Subscription pricing, artisan/client subscriptions, payments, and webhook
  processing persistence.
- Chargily checkout client and secure webhook endpoint.
- HMAC-SHA256 signature validation, raw-body size limiting, webhook idempotency,
  processing claims, stale-claim recovery, and retention cleanup.
- Subscription activation, cancellation, revocation, renewal reminders, and
  expiry lifecycle scheduling.
- Manual grants separated from cash-revenue analytics.

### Source snapshots

`src/main/java/com/project/souklab/controller/subscription/ChargilyWebhookController.java`

```java
@PostMapping("/webhook")
public ResponseEntity<Void> webhook(@RequestBody byte[] rawBody,
        @RequestHeader(value = SIGNATURE_HEADER, required = false) String signature) {
    if (signature == null || signature.isBlank()
            || rawBody == null
            || rawBody.length > appProperties.getChargily().getRequestBodyLimit()) {
        return ResponseEntity.badRequest().build();
    }
    try {
        webhookService.process(rawBody, signature);
        return ResponseEntity.ok().build();
    } catch (InvalidWebhookSignatureException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (MalformedWebhookException exception) {
        return ResponseEntity.badRequest().build();
    }
}
```

`src/main/java/com/project/souklab/service/subscription/SubscriptionLifecycleService.java`

```java
@Scheduled(fixedDelayString = "${app.subscription.lifecycle-interval}")
@Transactional
public void processLifecycle() {
    LocalDateTime now = LocalDateTime.now(
            clock.withZone(ZoneId.of(
                    appProperties.getSubscription().getLifecycleTimeZone())));
    expirePendingPayments(now);
    PageRequest batch = PageRequest.of(0,
            appProperties.getSubscription().getLifecycleBatchSize());
    artisanSubscriptions.findByStatusAndExpiresAtBefore(
            SubscriptionStatus.ACTIVE, now, batch).forEach(this::expire);
    clientSubscriptions.findByStatusAndExpiresAtBefore(
            SubscriptionStatus.ACTIVE, now, batch).forEach(this::expire);
    sendReminders(now);
    releaseStaleWebhookClaims(now);
    releaseExpiredWebhookLogs(now);
}
```

## Phase 10 — Moderation, analytics, and production hardening

### 10.1 User and formateur moderation

- Admin user listing, sorting, pagination, pending approval, bulk approval,
  permanent bans, timeouts, timeout lapse, unban, and reinstatement
  notifications.
- Effective account status is resolved before login and listing.
- Formateur governance from Phase 6 is exposed through admin controls.

Source: `UserManagementController`, `UserManagementService`,
`AdminFormateurController`, and `NotificationType.User`.

### 10.2 Asynchronous analytics

- `OVERVIEW`, `GROWTH`, `ENGAGEMENT`, `MODERATION`, `CONTENT_LEARNING`,
  `SUBSCRIPTIONS_PAYMENTS`, `OPERATIONAL`, `TIME_SERIES`, and `CSV_EXPORT`
  report families.
- Date ranges, day/week/month/quarter buckets, filters, sorting, pagination,
  JSON, CSV, owner isolation, permission snapshots, artifacts, and failure
  metadata.
- `/api/v1/admin/analytics/*` and compatibility alias
  `/api/v1/admin/stats/*`.
- Transactional MariaDB outbox, RabbitMQ relay with publisher confirms,
  retries/backoff/DLQ, idempotent consumer markers, daily rollups, bounded
  backfill/rebuild, retention cleanup, audit records, and STOMP job metadata
  notifications.

### Analytics API snapshot

`src/main/java/com/project/souklab/controller/analytics/AnalyticsJobController.java`

```java
@RestController
@RequestMapping({"/api/v1/admin/analytics", "/api/v1/admin/stats"})
@PreAuthorize("@accessControl.canViewAnalytics(authentication)")
public class AnalyticsJobController {
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<AnalyticsJobResponse>> submit(
            @Valid @RequestBody AnalyticsJobRequest request,
            Authentication authentication) {
        boolean financial = authentication.getAuthorities().stream()
                .anyMatch(Permission.Financial.ADMIN::matches);
        return ResponseEntity.accepted().body(ApiResponse.success(
                service.submit(request, authentication.getName(), financial),
                "Analytics job queued."));
    }

    @GetMapping("/jobs/{id}/result")
    public ResponseEntity<ApiResponse<AnalyticsResult>> result(
            @PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.result(id, authentication.getName())));
    }
}
```

### Event taxonomy snapshot

`src/main/java/com/project/souklab/analytics/AnalyticsEvent.java`

```java
public static List<Type> all() {
    return List.of(
            Registration.CREATED,
            Authentication.Login.SUCCEEDED,
            User.APPROVED,
            User.Timeout.EVENT,
            Profile.VIEW,
            Message.SENT,
            Formation.Moderation.APPROVED,
            Review.SUBMITTED,
            Report.RESOLVED,
            Feed.Post.PUBLISHED,
            Payment.State.TRANSITION,
            Subscription.RENEWAL);
}

public enum Report implements Type {
    SUBMITTED("REPORT_SUBMITTED"), RESOLVED("REPORT_RESOLVED");
    private final String value;
}
```

### Permission taxonomy snapshot

`src/main/java/com/project/souklab/security/Permission.java`

```java
public interface Permission extends EnumValue, GrantedAuthority {
    @JsonValue
    String value();

    @Override
    default String getAuthority() {
        return value();
    }
}

enum Analytics implements Permission {
    ADMIN("permission:analytics:admin");
}

enum Financial implements Permission {
    ADMIN("permission:financial:admin");
}
```

The `GrantedAuthority` API requires a string at the Spring Security boundary;
inside application checks the permission enum identity/matcher is used. The
stable string remains for database/API compatibility.

### Transactional outbox and idempotent rollups snapshot

`src/main/java/com/project/souklab/analytics/AnalyticsOutboxRelay.java`

```java
rabbitTemplate.invoke(operations -> {
    operations.convertAndSend(properties.getExchange(),
            properties.getRoutingKey(), event.getPayloadJson());
    operations.waitForConfirmsOrDie(
            properties.getConfirmTimeout().toMillis());
    return null;
});
event.setStatus(OutboxStatus.PUBLISHED);
repository.save(event);
```

`src/main/java/com/project/souklab/analytics/AnalyticsEventConsumer.java`

```java
if (processed.existsByEventId(id)) return;
rollups.incrementEventKpi(day,
        new AnalyticsEventRollupKey(day, type).databaseKey());
AnalyticsProcessedEvent marker = new AnalyticsProcessedEvent();
marker.setEventId(id);
processed.save(marker);
```

### 10.3 Operational hardening

- Public minimal liveness/readiness and private detailed health.
- MariaDB/Flyway, Redis, RabbitMQ, Elasticsearch, object storage, ClamAV, and
  configured payment-capability health indicators.
- Private Prometheus metrics and private generated OpenAPI/Swagger.
- Per-IP and authenticated per-user rate limits with endpoint classes for
  authentication, public API, admin API, analytics jobs/results, CSV, and
  Chargily webhook.
- Configuration validation for policy ranges, durations, queue topology,
  storage, health, OpenAPI, retention, and rate limits.

### OpenAPI snapshot

`src/main/java/com/project/souklab/config/OpenApiConfiguration.java`

```java
@Bean
OpenAPI souklabOpenApi() {
    return new OpenAPI()
            .info(new Info().title(properties.getTitle())
                    .version(properties.getVersion())
                    .description("Private administrator and platform API contract."))
            .components(new Components().addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP)
                            .scheme("bearer").bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

## Database migration history

The current Flyway chain is:

```text
V0__baseline_schema.sql
V1__phase7_social.sql
V2__authorization_permissions.sql
V3__phase8_messaging.sql
V4__production_query_indexes.sql
V5__phase9_subscriptions_payments.sql
V6__phase10_analytics.sql
V7__phase10_analytics_outbox.sql
V8__phase10_analytics_audit_action.sql
V9__phase10_analytics_artifacts.sql
V10__phase10_payment_origin.sql
V11__phase10_outbox_retry_schedule.sql
V12__phase10_analytics_maintenance_jobs.sql
V13__phase10_report_resolution_time.sql
V14__phase10_financial_audits.sql
V15__admin_catalog_permission.sql
```

The Phase 10 and 11 local harnesses have validated and applied the complete chain to a
fresh MariaDB schema.

## Phase 11 — Admin catalog taxonomy CRUD and reference seeding

### Delivered capabilities

- Full administrative CRUD endpoints under `/api/v1/admin/catalog/**` for:
  - Techniques (`/techniques/**`)
  - Epoques (`/epoques/**`)
  - Regions (`/regions/**`)
  - Job Categories & Subcategories (`/categories/**`, `/subcategories/**`)
  - Material Families & Materials (`/material-families/**`, `/materials/**`)
- Dedicated `permission:admin:catalog` (`Admin.CATALOG`) permission controlling write access.
- Automatic slug generation via `SlugUtils.toSlug(name)` with duplicate slug detection (`409 Conflict`).
- Two-tier foreign key integrity and deletion protection: deleting a parent Category with subcategories or Material Family with materials is prevented (`409 Conflict`).
- Self-referencing recursive hierarchy cycle guard in `RegionRepository.findAncestorIds` using recursive CTE.
- Real-time Caffeine cache eviction (`@CacheEvict`) on catalog mutations.
- Immutable audit log emission (`CATALOG_ITEM_CREATED`, `CATALOG_ITEM_UPDATED`, `CATALOG_ITEM_DELETED`).
- Seeded verbatim reference taxonomies via `DataSeeder`:
  - 8 French building trades / artisanat categories with 37 subcategories.
  - 6 Mediterranean material families with 25 materials.
  - 58 Algerian wilayas and regional administrative tree.
  - 14 historical craftsmanship epoques.
  - 20 traditional craftsmanship techniques.

## API and documentation artifacts

- `docs/API_SPEC.md` — REST contracts and common response behavior.
- `docs/dev/POSTMAN_API_REFERENCE.md` — admin, analytics, and phase acceptance
  examples.
- `docs/dev/PHASE10_VERIFICATION.md` — local Docker-backed evidence and scope
  boundary.
- `docs/RELEASE_CHECKLIST.md` — production/CI release gates.
- `docs/dev/PHASE10_DEFERRED_RELEASE_PLAN.md` — future-agent execution plan for
  deferred release work.
- `.github/workflows/production-verification.yml` — hosted verification
  workflow.

## Verification evidence currently available

The recorded local Docker-backed run has validated:

- fresh MariaDB/Flyway migration application through V13;
- RabbitMQ durable topology, confirms, delivery, and idempotency;
- Redis-backed rate-limit integration;
- MinIO storage tests;
- analytics configuration and event/rollup code paths;
- complete Maven suite at the recorded baseline:
  `Tests run: 1223, Failures: 0, Errors: 0, Skipped: 4`;
- source hygiene and migration checks.

A later verification run from the latest enum-taxonomy commit is the stronger
evidence when it completes. Hosted CI, production backup/restore, immutable
deployment identity, deployed readiness/Prometheus, and cross-instance
rate-limit evidence remain environment-dependent and are intentionally not
represented as completed here.

## Handoff rules for future changes

1. Inspect the current source and tests before editing; do not rely on this
   historical document as a substitute for the code.
2. Preserve API/database wire values when refactoring enum structure.
3. Keep imports explicit and avoid new inline fully-qualified references or
   anonymous implementation classes.
4. Add or update focused tests before broad verification.
5. Commit related changes in batches and push each coherent batch.
6. Update the relevant evidence document only with reproducible results.
7. Never check a production release box without the corresponding operator or
   hosted-system evidence.
