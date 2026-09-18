package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Storage storage = new Storage();
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Admin admin = new Admin();
    private Email email = new Email();
    private Mailersend mailersend = new Mailersend();
    private Chargily chargily = new Chargily();
    private OAuth oauth = new OAuth();
    private Relay relay = new Relay();
    private RateLimit rateLimit = new RateLimit();
    private AuthConfig auth = new AuthConfig();
    private ArtisanConfig artisan = new ArtisanConfig();
    private Search search = new Search();
    private Async async = new Async();
    private Cache cache = new Cache();
    private SupportConfig support = new SupportConfig();
    private Notification notification = new Notification();
    private Feed feed = new Feed();
    private Directory directory = new Directory();

    /**
     * Formations and masterclasses configuration bound to {@code app.formation.*}.
     */
    private FormationConfig formation = new FormationConfig();
    private Chat chat = new Chat();

    @Data
    public static class Notification {
        private int maxMessageLength;
    }

    @Data
    public static class Feed {
        private int maxMediaPerPost;
        private List<String> allowedImageMimeTypes;
    }

    @Data
    public static class Directory {
        private int defaultPageIndex;
        private int defaultPageSize;
        private int minPageSize;
        private int maxPageSize;
    }


    @Data
    public static class Relay {
        private String host;
        private int port;
        private String clientLogin;
        private String clientPasscode;
        private String systemLogin;
        private String systemPasscode;
    }

    @Data
    public static class Chat {
        private int messageMaxLength;
        private int attachmentMaxCount;
        private int minPageSize;
        private int defaultPageSize;
        private int maxPageSize;
        private Duration cursorLifetime;
        private Duration typingEventInterval;
        private String websocketProtocolVersion;
        private String applicationDestinationPrefix;
        private String userDestinationPrefix;
        private String messageDestinationPrefix;
        private String eventDestination;
        private String presenceDestination;
        private String notificationDestination;
        private String websocketEndpoint;
        private String brokerDestinationPrefixes;
        private String unresolvedUserDestination;
        private String userRegistryBroadcast;
    }

    @Data
    public static class Jwt {
        private String secret;
        private Long accessTokenExpirationMs;
        private Long refreshTokenExpirationMs;
    }

    @Data
    public static class Storage {
        private String uploadDir;
        private String thumbnails;
        private String indexes;

        /**
         * Route prefix for public or authenticated file streaming endpoints (default: /api/v1/files/).
         */
        private String fileServingPrefix;

        /**
         * Returns the configured file-serving route prefix normalised with a guaranteed trailing slash.
         * Callers should use this instead of rolling their own prefix resolution.
         *
         * @return normalised route prefix ending with {@code /}
         */
        public String resolveFileServingPrefix() {
            if (fileServingPrefix == null || fileServingPrefix.isBlank()) {
                throw new IllegalStateException("app.storage.file-serving-prefix must be configured");
            }
            return fileServingPrefix.endsWith("/") ? fileServingPrefix : fileServingPrefix + "/";
        }

        /**
         * Resolves an internal storage key to its public or CDN accessible URL representation.
         *
         * @param storageKey the raw storage key (e.g. UUID filename)
         * @return the fully qualified or relative public file serving URL, or null if key is null or blank
         */
        public String toUrl(String storageKey) {
            if (storageKey == null || storageKey.isBlank()) {
                return null;
            }
            return resolveFileServingPrefix() + storageKey;
        }
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }

    @Data
    public static class Admin {
        private String defaultEmail;
        private String defaultPassword;
        private boolean bootstrapEnabled;
        private String defaultBanReason;
        private String defaultTimeoutReason;
    }

    @Data
    public static class Email {
        private boolean useSmtp;
    }

    @Data
    public static class Mailersend {
        private String apiKey;
        private String apiUrl;
        private String senderEmail;
        private String senderName;
    }

    @Data
    public static class Chargily {
        private String apiKey;
        private String secretKey;
        private String mode;
        private String webhookSecret;
    }

    @Data
    public static class OAuth {
        private Google google = new Google();
        private int intentCookieMaxAgeSeconds;

        @Data
        public static class Google {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
        }
    }

    @Data
    public static class SupportConfig {
        private String email;
        private String contactMessage;
    }

    /**
     * Global API rate-limiting configuration bound to {@code app.rate-limit.*}.
     * Defaults are supplied via environment variables in {@code application.properties};
     * no Java-side defaults are declared here.
     */
    @Data
    public static class RateLimit {
        private boolean enabled;
        private int capacity;
        private Duration refillDuration;
        private Cache cache = new Cache();

        /**
         * In-memory cache configuration for global rate limiting buckets.
         */
        @Data
        public static class Cache {
            private long maximumSize;
            private Duration expireAfterAccess;
        }
    }

    /**
     * Authentication and security policy configuration bound to {@code app.auth.*}.
     */
    @Data
    public static class AuthConfig {
        private LockoutConfig lockout = new LockoutConfig();
        private VerificationConfig verification = new VerificationConfig();

        /**
         * Account lockout policy configuration bound to {@code app.auth.lockout.*}.
         */
        @Data
        public static class LockoutConfig {
            private int maxAttempts;
            private int durationMinutes;
        }

        /**
         * One-time verification token (OTP) policy configuration bound to {@code app.auth.verification.*}.
         */
        @Data
        public static class VerificationConfig {
            private int maxAttempts;
            private int expirationMinutes;
            private int codeLength;
        }
    }

    /**
     * Artisan showcase and certification configuration bound to {@code app.artisan.*}.
     */
    @Data
    public static class ArtisanConfig {
        private GalleryConfig gallery = new GalleryConfig();
        private CertificationConfig certification = new CertificationConfig();
        private FormateurConfig formateur = new FormateurConfig();

        /**
         * Formateur status and reapplication configuration bound to {@code app.artisan.formateur.*}.
         */
        @Data
        public static class FormateurConfig {
            private long reapplyCooldownDays;
        }

        /**
         * Gallery showcase portfolio configuration bound to {@code app.artisan.gallery.*}.
         */
        @Data
        public static class GalleryConfig {
            private int maxImages;
            private DataSize maxFileSize;
            private List<String> allowedMimeTypes;
        }

        /**
         * Official certification and accreditation configuration bound to {@code app.artisan.certification.*}.
         */
        @Data
        public static class CertificationConfig {
            private int maxCount;
            private DataSize maxFileSize;
            private List<String> allowedMimeTypes;
        }
    }

    /**
     * Search and indexing configuration bound to {@code app.search.*}.
     * Defaults are supplied via environment variables in {@code application.properties};
     * no Java-side defaults are declared here.
     */
    @Data
    public static class Search {
        private boolean enabled;
        private String uris;
        private String username;
        private String password;
        private int connectionTimeout;
        private int readTimeout;
        private String indexPrefix;
        private String schemaManagement;
        private boolean syncOnStartup;
        private MassIndexing massIndexing = new MassIndexing();

        @Data
        public static class MassIndexing {
            private int threadsToLoadObjects;
            private int batchSizeToLoadObjects;
            private int idFetchSize;
        }
    }

    /** Runtime configuration for application-managed asynchronous executors. */
    @Data
    public static class Async {
        private Executor application = new Executor();
        private Executor workflow = new Executor();

        @Data
        public static class Executor {
            private int corePoolSize;
            private int maxPoolSize;
            private int queueCapacity;
            private String threadNamePrefix;
        }
    }

    /** Runtime configuration for the in-memory catalog cache. */
    @Data
    public static class Cache {
        private Duration expireAfterWrite;
        private long maximumSize;
    }

    /**
     * Formations and masterclasses configuration bound to {@code app.formation.*}.
     */
    @Data
    public static class FormationConfig {

        /**
         * Formation thumbnail image configuration bound to {@code app.formation.thumbnail.*}.
         */
        private ThumbnailConfig thumbnail = new ThumbnailConfig();

        /**
         * Formation course attachments configuration bound to {@code app.formation.file.*}.
         */
        private FileConfig file = new FileConfig();

        /**
         * Formation cancellation policy configuration bound to {@code app.formation.cancellation.*}.
         */
        private CancellationConfig cancellation = new CancellationConfig();

        /**
         * Formation discovery and enrollment pagination configuration bound to {@code app.formation.pagination.*}.
         */
        private PaginationConfig pagination = new PaginationConfig();

        /**
         * Default ISO currency code for formations (default: DZD).
         */
        private String defaultCurrency;

        /**
         * Configuration for formation thumbnail images.
         */
        @Data
        public static class ThumbnailConfig {

            /**
             * Maximum allowable file size for formation thumbnails (default: 10MB).
             */
            private DataSize maxFileSize;

            /**
             * Permitted MIME types for formation showcase thumbnails.
             */
            private List<String> allowedMimeTypes;
        }

        /**
         * Configuration for formation downloadable course files and syllabus attachments.
         */
        @Data
        public static class FileConfig {

            /**
             * Maximum number of course files allowed per formation (default: 10).
             */
            private int maxCount;

            /**
             * Maximum allowable file size for individual course files (default: 25MB).
             */
            private DataSize maxFileSize;

            /**
             * Permitted MIME types for formation course files and syllabus attachments.
             */
            private List<String> allowedMimeTypes;
        }

        /**
         * Configuration for formation enrollment cancellation rules.
         */
        @Data
        public static class CancellationConfig {

            /**
             * Minimum cutoff deadline in hours prior to scheduled masterclass start time (default: 24).
             */
            private int deadlineHours;
        }

        /**
         * Configuration for formation catalog and enrollment pagination.
         */
        @Data
        public static class PaginationConfig {

            /**
             * Default page size for catalog listings and enrollment history (default: 10).
             */
            private int defaultPageSize;
        }
    }
}
