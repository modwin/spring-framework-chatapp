# Architecture and organization review

Review date: 2026-08-10

## Executive assessment

The project had a reasonable learning-project skeleton, but its public surface was ahead of its actual business logic. The most serious problems were an unauthenticated login bypass, disabled CSRF protection, a Google credential in local configuration and Git history, an outdated frontend/backend contract, and container definitions that did not start a complete system.

The implemented refactor brings the working scope to a coherent account-and-friendship application. Security boundaries, API contracts, schema ownership, tests, and local deployment now describe the same system. Messaging has been quarantined at the persistence layer instead of being exposed through incomplete controllers.

## Findings and actions

| Severity | Area | Finding | Resolution |
| --- | --- | --- | --- |
| Critical | Secrets | A Google OAuth client secret was present in an ignored source configuration file and remains in Git history. | Removed secret-bearing configuration from the working tree, added environment-driven OAuth configuration, and ignored `.env`/`application-local.yml`. The credential still must be revoked and rotated externally. |
| Critical | Authentication | `/api/users/login/auth` accepted identity data without verifying credentials. | Removed the endpoint and DTO; local login now goes through Spring Security's `AuthenticationManager`. |
| High | Web security | CSRF was disabled for a session-authenticated browser application, and route authorization was broadly permissive. | Restored CSRF, added a token endpoint, introduced an explicit allowlist, denied unspecified routes, rotated session IDs, and returned JSON problem responses for 401/403. |
| High | API contract | The React app targeted stale endpoints and presented unfinished functionality as available. | Added typed API modules, aligned every request with the backend, and limited UI claims to authentication and friendships. |
| High | Persistence | Hibernate implicitly owned schema creation, while PostgreSQL runtime support and migration control were incomplete. | Added PostgreSQL runtime support, Flyway, an initial migration, schema validation, and a PostgreSQL/Testcontainers compatibility test. |
| High | Friendships | The original relationship model did not clearly represent pending/accepted direction or authorization. | Modeled requester, recipient, and status explicitly; only recipients can accept and only participants can remove. Added unordered-pair uniqueness at the database layer. |
| Medium | API design | Persistence entities and over-broad DTOs leaked concepts across layers; exception semantics were inconsistent. | Added request/response records, a mapper boundary, validation, and centralized problem-detail handling. |
| Medium | Frontend structure | A stale monolithic app mixed presentation, transport, and workflow state. | Split API contracts/client, authentication, dashboard, friendship, and alert components; retained workflow orchestration in `App`. |
| Medium | Delivery | Backend/frontend Docker files used inconsistent runtimes and the frontend-only Compose file omitted PostgreSQL and the backend topology. | Standardized Java 21 and Node 24 builds, used `npm ci`, added non-root backend runtime, and consolidated the stack in root `compose.yaml`. |
| Medium | Verification | Coverage did not protect the security boundary or the full friendship lifecycle. | Added controller, service, browser-component, API-client, and optional real-PostgreSQL migration tests. |
| Low | Repository hygiene | Generated template documentation, dead controllers/services/DTOs, redundant repository methods, and an unused servlet initializer obscured responsibilities. | Removed or replaced these items and documented the supported scope. |

## Current responsibility distribution

The backend uses a conventional layered layout:

- `controller`: HTTP routing, validation boundary, response status, and problem mapping.
- `service`: authentication orchestration and business authorization rules.
- `persistence/model`: relational domain state only.
- `persistence/repository`: query and persistence operations.
- `dto`: API-specific input/output contracts.
- `security`: one explicit Spring Security policy.
- `util`: the friendship state enum and stateless response mapper.

For the project's current size, this is understandable and maintainable. If chat, moderation, notifications, or administration are added, prefer package-by-feature boundaries such as `account`, `friendship`, and `chat`, with each feature owning its API, application service, and persistence code. Changing package layout now would create widespread churn without a current business benefit.

The frontend now follows a similarly small, appropriate split:

- `api`: transport behavior and shared contracts.
- `components`: focused UI sections with typed callbacks.
- `App.tsx`: session discovery and cross-component workflow orchestration.
- colocated tests for API behavior and application states.

Introduce a router and feature folders only when there are real navigable screens. Adding those abstractions to the current single-screen application would be premature.

## Deliberately deferred recommendations

1. Complete the chat aggregate before restoring chat endpoints. Define membership changes, ownership transfer, message editing/deletion, pagination, and authorization first; then add real-time transport as a delivery concern rather than as the source of truth.
2. Move from shared DTO contracts to a generated OpenAPI client once the API grows enough that manual synchronization becomes error-prone.
3. Add rate limiting, email verification, password reset, audit events, and session/device management before treating local accounts as internet-production ready.
4. Add observability beyond health probes (structured logs, metrics, traces, and alerting) when a deployment target exists.
5. Use a managed secret store and non-default database credentials for any shared or production environment.
6. Consider replacing Lombok on persistence entities if debugging/build-tool transparency becomes more valuable than the small reduction in boilerplate.
7. Rename the mixed-case Java package `com.modwin.ModwinChatApp` only as a planned repository-wide migration; Java conventions favor an all-lowercase package, but this is not worth a risky incidental rewrite.
8. Treat Spring Boot 4 and TypeScript 7 as separate major-version migrations. The current Java 21/Spring Boot 3.5 line and Node 24/TypeScript 6 toolchain are deliberately conservative, supported baselines; major upgrades should have their own compatibility and regression pass.

## Known scope boundaries

- The application is a secure local/portfolio baseline, not a production-ready messaging service.
- `Chat` and `Message` entities and repositories are retained because their schema is coherent, but no service/controller/UI depends on them.
- Google login is opt-in through the `oauth` Spring profile, so local development does not require third-party credentials.
- The H2 suite provides fast feedback; the Docker-aware test is the authority for PostgreSQL migration compatibility.
