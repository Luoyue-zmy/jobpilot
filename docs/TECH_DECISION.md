# Jobpilot Initial Technical Decision

## Decision

Use a lightweight local-first scaffold:

- Java 17 + Spring Boot 2.6.6 backend.
- Dependency-free static frontend prototype.
- No automatic dependency download during scaffold creation.

## Reason

The PRD recommends Spring Boot 3, Vue 3, MyBatis-Plus, MySQL, Redis, JWT, and Docker Compose. The current machine already has Java 17, Maven, Node 16, Spring/Java VSCode extensions, and cached Spring Boot 2.6.6 dependencies. npm is configured for offline cache and the Vite/Vue packages are not cached, so a Vue scaffold would require network and extra storage.

## Upgrade Path

1. Keep backend package boundaries stable: `controller`, `service`, `model`, `dto`, `config`.
2. Replace in-memory services with MyBatis-Plus mappers and MySQL schema.
3. Add JWT/Spring Security after the first job CRUD loop is stable.
4. Replace the static frontend with Vue 3 + TypeScript + Vite when dependency installation is available.
5. Add Docker Compose after MySQL and Redis are actually used.

