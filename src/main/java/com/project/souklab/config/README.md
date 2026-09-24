# Configuration Package (`com.project.souklab.config`)

Centralizes framework configurations, custom Spring Beans, security filter setup, asynchronous execution, caching policies, and application property bindings.

---

## Key Responsibilities

- Configures Spring Security filter chains, stateless JWT authentication, hardened security headers (CSP, Permissions-Policy, Referrer-Policy, Frame-Options), and CORS policies.
- Enforces the Principle of Least Privilege on public read paths (`/feed`, `/catalog/**`, `/public/**`, `/subscriptions/plans`, `/artisans/*/reviews`), restricting unauthenticated access strictly to `HttpMethod.GET`.
- Sets up asynchronous task executors (`AsyncConfig`) and deterministic clocks (`ClockConfig`).
- Configures Caffeine in-memory caches for reference taxonomy data; security rate limits use the shared Bucket4j backend in production.
- Binds externalized configuration properties (`AppProperties`, `AvatarProperties`) and framework-level proxy header strategy (`server.forward-headers-strategy=framework`).
- Configures WebSocket endpoints, STOMP message routing, and authentication handshakes (`WebSocketConfig`).
- Seeds canonical permissions and reference data; administrator bootstrap is explicitly property-gated (`DataSeeder`).
- Harmonizes outer HTTP status codes with inner envelope codes (`ApiResponseCodeAdvice`).
- Manages Hibernate Search 8.2.2.Final with Elasticsearch analysis and indexing lifecycle in subpackage [`search`](search/README.md).

---

## Classes Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`SecurityConfig`](SecurityConfig.java) | `@Configuration` | Configures `SecurityFilterChain`, CORS rules, hardened headers (CSP, Permissions-Policy), method-specific public endpoint permissions, and filter order. |
| [`AppProperties`](AppProperties.java) | `@ConfigurationProperties(prefix = "app")` | Binds JWT secrets, storage, search, formation, support, and authentication configuration. |
| [`AvatarProperties`](AvatarProperties.java) | `@ConfigurationProperties(prefix = "avatar")` | Configures avatar quotas, MIME policy, and upload rate limiting. |
| [`CacheConfig`](CacheConfig.java) | `@Configuration`, `@EnableCaching` | Configures Caffeine cache manager and names for Wilayas, categories, materials, epoques, and techniques. |
| [`AsyncConfig`](AsyncConfig.java) | `@Configuration`, `@EnableAsync` | Configures thread pools for non-blocking operations (audit logging, email dispatch). |
| [`ClockConfig`](ClockConfig.java) | `@Configuration` | Exposes a `java.time.Clock` bean for time-dependent operations (token expiration, cooldown tracking). |
| [`PasswordEncoderConfig`](PasswordEncoderConfig.java) | `@Configuration` | Exposes a `BCryptPasswordEncoder` bean for secure credential hashing. |
| [`ApiResponseCodeAdvice`](ApiResponseCodeAdvice.java) | `@ControllerAdvice` | Intercepts HTTP response bodies to synchronize outer HTTP status codes with inner `ApiResponse.code`. |
| [`DataSeeder`](DataSeeder.java) | `@Component`, `CommandLineRunner` | Seeds canonical permissions and reference data; creates an admin only when `APP_ADMIN_BOOTSTRAP_ENABLED=true`. |
| [`ConfigurationPolicyValidator`](ConfigurationPolicyValidator.java) | `@Component` | Rejects invalid limits, MIME policies, CORS credentials/origin combinations, and unsafe production storage settings. |
| [`WebSocketConfig`](WebSocketConfig.java) | `@Configuration`, `@EnableWebSocketMessageBroker` | Configures STOMP messaging, `/ws` endpoint, user destination prefixes, and external broker relays. |
| [`WebSocketAuthInterceptor`](WebSocketAuthInterceptor.java) | `ChannelInterceptor` | Authenticates STOMP `CONNECT` frames by validating Bearer JWT tokens in connect headers. |
| [`WebClientConfig`](WebClientConfig.java) | Class | Foundation configuration class for external HTTP client integrations. |

---

## Subpackages

| Subpackage | Responsibility |
| :--- | :--- |
| [`search`](search/README.md) | Custom Elasticsearch analysis configurer and startup mass indexing runner for Hibernate Search. |
