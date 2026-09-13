package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.ArrayList;
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

    /**
     * Formations and masterclasses configuration bound to {@code app.formation.*}.
     */
    private FormationConfig formation = new FormationConfig();


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
        private String fileServingPrefix = "/api/v1/files/";
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    public static class Admin {
        private String defaultEmail;
        private String defaultPassword;
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

        @Data
        public static class Google {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
        }
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

        /**
         * Account lockout policy configuration bound to {@code app.auth.lockout.*}.
         */
        @Data
        public static class LockoutConfig {
            private int maxAttempts = 5;
            private int durationMinutes = 15;
        }
    }

    /**
     * Artisan showcase and certification configuration bound to {@code app.artisan.*}.
     */
    @Data
    public static class ArtisanConfig {
        private GalleryConfig gallery = new GalleryConfig();
        private CertificationConfig certification = new CertificationConfig();

        /**
         * Gallery showcase portfolio configuration bound to {@code app.artisan.gallery.*}.
         */
        @Data
        public static class GalleryConfig {
            private int maxImages = 20;
            private DataSize maxFileSize = DataSize.ofMegabytes(10);
            private List<String> allowedMimeTypes = new ArrayList<>(List.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ));
        }

        /**
         * Official certification and accreditation configuration bound to {@code app.artisan.certification.*}.
         */
        @Data
        public static class CertificationConfig {
            private int maxCount = 10;
            private DataSize maxFileSize = DataSize.ofMegabytes(15);
            private List<String> allowedMimeTypes = new ArrayList<>(List.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            ));
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
         * Default ISO currency code for formations (default: DZD).
         */
        private String defaultCurrency = "DZD";

        /**
         * Configuration for formation thumbnail images.
         */
        @Data
        public static class ThumbnailConfig {

            /**
             * Maximum allowable file size for formation thumbnails (default: 10MB).
             */
            private DataSize maxFileSize = DataSize.ofMegabytes(10);

            /**
             * Permitted MIME types for formation showcase thumbnails.
             */
            private List<String> allowedMimeTypes = new ArrayList<>(List.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ));
        }

        /**
         * Configuration for formation downloadable course files and syllabus attachments.
         */
        @Data
        public static class FileConfig {

            /**
             * Maximum number of course files allowed per formation (default: 10).
             */
            private int maxCount = 10;

            /**
             * Maximum allowable file size for individual course files (default: 25MB).
             */
            private DataSize maxFileSize = DataSize.ofMegabytes(25);

            /**
             * Permitted MIME types for formation course files and syllabus attachments.
             */
            private List<String> allowedMimeTypes = new ArrayList<>(List.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            ));
        }

        /**
         * Configuration for formation enrollment cancellation rules.
         */
        @Data
        public static class CancellationConfig {

            /**
             * Minimum cutoff deadline in hours prior to scheduled masterclass start time (default: 24).
             */
            private int deadlineHours = 24;
        }
    }
}


