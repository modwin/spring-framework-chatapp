# Spring Boot framework - modwin chatapp

A full-stack, containerized web application built with **Spring Boot**, **React**, and **PostgreSQL** — demonstrating dual-mode authentication (local + Google OAuth2/OIDC), a JPA-modeled friends network, and a production-style Docker/Nginx deployment topology.

**Note on the name:** the original goal was a full chat application. What's built and verified so far is the authentication and social-graph layer beneath it — the messaging layer itself is the current work in progress (see [Current Status](#current-status)).

## Tech Stack

| Layer      | Technology                                                        |
|------------|---------------------------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3, Spring Security 6, Spring Data JPA        |
| Frontend   | React 19, TypeScript, Vite                                          |
| Database   | PostgreSQL (runtime), H2 (test)                                     |
| Auth       | Local username/password (BCrypt) + Google OAuth2 / OIDC             |
| Infra      | Docker (multi-stage builds), Docker Compose, Nginx reverse proxy    |

## Current Status

### Implemented and working
- User registration and local login, passwords hashed with BCrypt
- Google OAuth2/OIDC login ("Sign in with Google")
- Session-based authentication via Spring Security, supporting both auth methods concurrently
- Friend system — add/remove friends by email, modeled as a bidirectional JPA `@ManyToMany` relationship
- User profile retrieval
- Fully containerized stack: backend, frontend (served via Nginx), and PostgreSQL each run in their own container
- Integration test covering the register → login → invalid-login flow

### Not yet implemented
- **Chat / messaging.** `Chat` and `Message` entities and repositories exist, but the service and controller layer for sending, storing, and retrieving messages is not yet built.
- **Real-time delivery** (WebSocket/STOMP) for messages, once the above is in place.
- **Profile editing** — profiles are currently read-only after registration.

## Architecture

- The React frontend is built and served as static assets via Nginx, which reverse-proxies `/api`, `/oauth2`, and `/login` through to the Spring Boot backend (see `nginx.conf`).
- The backend exposes REST endpoints under `/api/users/**` and handles both local and OAuth2/OIDC authentication through a single Spring Security filter chain.
- PostgreSQL persists `User`, `Role`, `Chat`, and `Message` entities. The `Chat`/`Message` tables exist but aren't yet wired to any endpoint.

## Running Locally

**Prerequisites:** Docker and Docker Compose.

```bash
docker compose up --build
```

This builds and starts the backend, frontend, and PostgreSQL containers together. By default the backend listens on `8081` inside its container and the frontend's Nginx serves on `80` inside its container — check your local `docker-compose.yml` for the exact host port mappings, since these can be environment-specific.

**Google OAuth2 login** requires a `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` to be set (see `spring.security.oauth2.client.registration.google.*` in `application.properties`, which is git-ignored — you'll need to supply your own via a `.env` file or environment variables).

## Roadmap

- [ ] Chat message sending/receiving (`ChatService`, `ChatController`)
- [ ] Real-time delivery via WebSocket/STOMP
- [ ] Frontend chat UI
- [ ] Profile editing
- [ ] Expanded test coverage for friend endpoints, and for chat once implemented

## Notable Engineering Details

- **Dual authentication paths** (local credentials + OAuth2/OIDC) coexisting cleanly in a single Spring Security configuration, rather than two separately bolted-on systems.
- **Bidirectional relationship modeling** in JPA for the friends graph.
- **Production-style deployment topology** — multi-stage Docker builds for both services, with Nginx handling static asset serving and reverse proxying rather than exposing the backend directly.
- **AI-agent-assisted development, human-reviewed:** the initial React/Vite frontend (auth forms, friend management UI) was built autonomously by [Junie](https://www.jetbrains.com/junie/) (JetBrains' agentic coding assistant in IntelliJ IDEA) while backend architecture, security configuration, and integration were designed and verified by hand — including catching and fixing JPA mapping issues in the AI-generated scaffolding.

## License

MIT — see [LICENSE](./LICENSE).
