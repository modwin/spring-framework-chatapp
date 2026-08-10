# Architecture and organization review

Review date: 2026-08-10

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
