# Modwin Chat

Modwin Chat is a full-stack account and friendship application built with Spring Boot, React, and PostgreSQL. The repository contains a working authentication and social-graph foundation; chat delivery is intentionally not exposed until its business rules are implemented.

## Current capabilities

- Local registration and login using BCrypt and server-side sessions
- Optional Google OpenID Connect login
- Authenticated profile retrieval
- Send, accept, decline/cancel, and remove friendship relationships
- CSRF protection and RFC 9457-style API problem responses
- Flyway-managed PostgreSQL schema
- Responsive React UI backed by typed API contracts

`Chat` and `Message` persistence models remain as future-facing domain groundwork. There are no chat endpoints or real-time delivery claims yet.

## Technology

| Area | Stack |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| Frontend | React 19, TypeScript 6, Vite 8 |
| Data | PostgreSQL 17, Flyway; H2 for fast tests |
| Delivery | Docker Compose, multi-stage images, Nginx reverse proxy |

## Run the complete stack

Prerequisites: Docker Desktop (or Docker Engine with Compose).

```bash
docker compose up --build
```

Open <http://localhost:3000>. PostgreSQL data is retained in the `postgres-data` volume. The backend is reachable through Nginx rather than published directly.

The checked-in defaults are intended only for local development. Copy `.env.example` to `.env` to override them. Do not use the default database password outside a local machine.

### Optional Google login

Set these values in `.env`:

```dotenv
SPRING_PROFILES_ACTIVE=oauth
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
FRONTEND_URL=http://localhost:3000
```

Configure the provider callback as `http://localhost:3000/login/oauth2/code/google`. Nginx forwards that route to Spring Security.

## Run during development

Start PostgreSQL first (the Compose database service is sufficient), then run the backend with Java 21:

```bash
./mvnw spring-boot:run
```

In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

The Vite development server proxies backend routes to port `8081`.

## Verification

```bash
./mvnw test
cd frontend
npm run lint
npm test
npm run build
```

The PostgreSQL migration test runs when Docker is available and is skipped otherwise. The remaining backend tests use an isolated H2 database.

## API summary

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/csrf` | Public | Obtain the session CSRF token |
| GET | `/api/auth/providers` | Public | Discover enabled login providers |
| POST | `/api/auth/register` | Public + CSRF | Create and sign in a local account |
| POST | `/api/auth/login` | Public + CSRF | Sign in a local account |
| POST | `/api/auth/logout` | Authenticated + CSRF | End the current session |
| GET | `/api/users/me` | Authenticated | Get the current profile |
| GET/POST | `/api/friendships` | Authenticated | List or create requests |
| PATCH | `/api/friendships/{id}/accept` | Recipient | Accept a pending request |
| DELETE | `/api/friendships/{id}` | Participant | Decline, cancel, or remove |

See [the architecture review](docs/architecture-review.md) for the design assessment, implemented corrections, and deferred recommendations.

## License

MIT — see [LICENSE](LICENSE).
