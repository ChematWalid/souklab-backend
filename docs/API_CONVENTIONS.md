# API Conventions

This is the canonical reference for response shapes, status codes, and
API design decisions for the Souklab backend. When in doubt, match this
doc rather than re-deriving a convention from scratch.

---

## 1. Response Envelope

Every response — success or error — uses `ApiResponse<T>`:

```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

- `code` (int): **always present**, never omitted. Stamped automatically
  by `ApiResponseCodeAdvice` (a `ResponseBodyAdvice`) from the real HTTP
  status just before serialization. No controller or service should ever
  set this manually — it can't drift from the actual response status
  because it's derived from it.
- `errorCode` (string): present **only** on business-exception responses
  (`AppException` subclasses), omitted via `@JsonInclude(NON_NULL)`
  everywhere else. A short slug for frontend branching (e.g.
  `RESOURCE_NOT_FOUND`, `FORBIDDEN`, `CONFLICT`).
- `errors` (map): present only on field-validation failures (422).

### Paginated responses

```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 85,
    "totalPages": 5,
    "last": false
  }
}
```

Use `@PageableDefault(size = 20)` on the controller method — do not build
`PageRequest` by hand. Page size is globally capped (see Section 4).

---

## 2. HTTP Status Code Table

| Scenario | Status | Notes |
|---|---|---|
| GET/PATCH/PUT — success with body | 200 | |
| POST — resource created | 201 | |
| DELETE / no-body success | 200 with `data: null` | This project does not use 204 — every response goes through `ApiResponse`, including deletes |
| Field validation failure | 422 | `MethodArgumentNotValidException` / `ConstraintViolationException`, includes `errors` map |
| Malformed/unreadable JSON | 400 | Genuine request-shape problems only |
| Unauthenticated | 401 | |
| **Permission mismatch** | **403** | Authenticated, but not allowed to do this. Covers: `@PreAuthorize` failures, "you don't have the profile this action requires," business-rule blocks (cooldowns, permanent blocks) |
| Not found | 404 | |
| **State conflict** | **409** | The action can't proceed because of *current state*, not because the request is invalid — duplicate-in-progress request, "you already have the thing you're requesting" |
| Rate limited | 429 | Includes `Retry-After: <seconds>` header computed from token-bucket refill estimation |
| Unexpected server error | 500 | Never leak stack traces or raw exception messages |

### The 400 vs 403 vs 409 decision rule

This distinction has been a recurring source of inconsistency — use this
rule going forward:

- **400** — the request itself is malformed (bad JSON, unparseable body).
  Field-level *validation* failures use 422, not 400.
- **403** — the caller is identified and authenticated, but isn't
  *allowed* to do this. Includes permission mismatches, missing required
  profile/state on the caller's own account, and policy blocks (cooldown,
  permanent block).
- **409** — the request is well-formed and the caller is allowed to make
  it, but it conflicts with the *current state of the target resource* —
  most often "you're trying to create/request something that already
  exists in that state" (duplicate pending request, already has the
  status being requested).

Worked examples from this codebase:
- Submitting a Formateur request while one is already `PENDING` → 409
  (a second identical request conflicts with the existing one)
- Submitting a Formateur request when already an approved Formateur → 409
  (the outcome you're requesting already exists)
- Submitting a Formateur request with no registered artisan profile → 403
  (permission/profile mismatch, not a state conflict)
- Submitting during an active cooldown or permanent block → 403
  (policy forbids the action)
- Admin hitting `PATCH /me` → 403 (no profile entity to patch)

### `@PreAuthorize` failures

Controllers use centralized permission predicates rather than role literals. Permission identifiers are the only authorization contract and are resolved from the database.

Spring Security's `AccessDeniedException` is handled explicitly in
`GlobalExceptionHandler` and mapped to the same envelope as every other
403 (`errorCode: "FORBIDDEN"`) — it does not fall through to a bare
"Access denied" response with no `errorCode`. Any new `@ExceptionHandler`
added to this project must use the `ApiResponse.error(errorCode, message)`
two-argument overload, never the message-only overload, or `errorCode`
silently disappears from the response via `@JsonInclude(NON_NULL)`.

---

## 3. URL & Resource Conventions

- `/api/v1/` prefix, hardcoded per-controller (see Section 5 on
  versioning — this is a deliberate current choice, not an oversight).
- Plural nouns for resources, kebab-case for multi-word paths
  (`formateur-requests`, `approve-bulk`).
- UUIDs as path IDs, never auto-increment integers.
- Actions that don't map to CRUD use a trailing verb sparingly
  (`/formateur-requests/{id}/approve`, `/artisans/{id}/formateur-grant`).

---

## 4. Pagination Limits

Set globally in `application.properties`:

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

A request for `?size=99999` is silently clamped to 100, never errors,
never returns more than the cap. This applies to every `Pageable`-backed
endpoint in the app — do not override per-controller without a specific
reason.

---

## 5. API Versioning — Deliberately Deferred

Spring Boot 4 / Framework 7 (confirmed running: Boot 4.0.8) has native
version-attribute routing (`version` attribute on mappings,
`spring.mvc.apiversion.*` config) as an alternative to hardcoding
`/api/v1/` as a literal path prefix.

**Decision: not adopted yet.** This project has exactly one API version
in existence, no `v2` planned, and no deprecation timeline — there is
currently zero observable difference between a hardcoded prefix and a
framework-resolved one. Migrating now would touch every controller's
mapping annotations for no present benefit.

**Revisit when:** a real `v2` of some endpoint is actually needed (a
breaking change that must coexist with `v1` for existing clients). At
that point, migrate as its own isolated, dedicated pass — not bundled
into unrelated feature work.

---

## 6. Jackson / JSON Handling

This project runs **Jackson 3** (`tools.jackson.databind`, not the
Jackson 2 `com.fasterxml.jackson` package). When writing new code:

- Inject the app's real configured `ObjectMapper` bean via constructor —
  never `new ObjectMapper()`. A second, uncustomized instance can
  serialize dates/fields differently from the rest of the app.
- Known cleanup item (not yet done, deliberately deferred): two existing
  classes (`ServletResponseUtil`, `AuthService`) declare their injected
  bean as `ObjectMapper` where Jackson 3 convention prefers the concrete
  `JsonMapper` type. Flagged for a future pass, not urgent.
- Do not use Jackson 2-era annotations/classes (`@JsonComponent`,
  `Jackson2ObjectMapperBuilderCustomizer`) — confirmed zero occurrences
  as of the last audit; keep it that way.

---

## 7. PATCH Semantics (Partial Updates)

Where an endpoint supports partial updates (e.g. `PATCH /api/v1/auth/me`),
this project uses **JSON Merge Patch** semantics via `JsonNode`:

- Field omitted from the request body → entity field is left untouched.
- Field explicitly present with value `null` → entity field is cleared
  to `null` in the database.
- Field present with a real value → validated (reusing the same Bean
  Validation rules as the equivalent "complete" DTO, never relaxed for
  PATCH) and applied.

Account-level fields (email, password, accountType, accountStatus) and
admin/workflow-controlled fields (e.g. `isTeacher`) are never accepted
through a self-service PATCH — they're excluded from the DTO entirely,
not merely validated-and-rejected.

---

## 8. General Rules Carried Through Every Phase

- Never swallow an exception silently — every catch block either
  re-throws a domain exception or logs with enough detail to diagnose
  from logs alone.
- No fully-qualified inline class references — use imports, even for
  framework classes.
- Scope changes to what was actually asked — shared infrastructure
  (`BaseEntity`, `GlobalExceptionHandler`, etc.) is never touched as a
  side effect of an unrelated feature without it being called out
  explicitly first.
- Secrets never hardcoded — config flows `.env` → `application.properties`
  → `AppProperties`, and test/scratch scripts with real credentials stay
  outside the repository entirely.
