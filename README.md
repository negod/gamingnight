# Gaming Night

Gaming Night is a web application for planning and running game-night competitions. It manages players, teams, games, competitions, generated matches, result entry, and leaderboards.

The stack is React, Vite, TypeScript, Tailwind CSS, Spring Boot 3, Java 21, PostgreSQL, Spring Data JPA, Hibernate, and Liquibase.

## Contents

- [Features](#features)
- [Repository Layout](#repository-layout)
- [Prerequisites](#prerequisites)
- [First-Time Setup](#first-time-setup)
- [Run Locally](#run-locally)
- [Database And Liquibase](#database-and-liquibase)
- [Environment Variables](#environment-variables)
- [Tests And Builds](#tests-and-builds)
- [Documentation](#documentation)
- [Deployment](#deployment)

## Features

- Manage players.
- Log in with role-based access control.
- Administer system users with `ADMIN` or `USER` roles.
- Tie each system user to exactly one player.
- Manage games with score-based or time-based ranking and sum or average calculations.
- Manage teams with globally unique team names.
- Create competitions with ordered games and assigned teams.
- Generate teams from selected players using seeded, non-repeating team names.
- Start competitions and generate round-robin matches.
- Enter match results per player.
- View per-game and total leaderboards for teams and players.
- Check backend health through Spring Boot Actuator.

## Repository Layout

```text
.
|-- backend/                   # Spring Boot API
|   |-- src/main/java/se/backede/
|   |   |-- domain/            # Domain models and repository ports
|   |   |-- application/       # Use cases, DTOs, and mappers
|   |   |-- infrastructure/    # Web controllers, JPA adapters, config
|   |   `-- shared/            # Framework-free shared code
|   `-- src/main/resources/
|       |-- application.yml
|       `-- db/changelog/      # Liquibase changelogs
|-- frontend/                  # React/Vite frontend
|   `-- src/
|       |-- app/               # Routing and app shell
|       |-- pages/             # Route screens
|       |-- features/          # Feature APIs and components
|       `-- shared/            # Shared API client, types, components
|-- docs/                      # Technical documentation
|-- docker-compose.yml         # Local PostgreSQL service
|-- package.json               # Root convenience scripts
`-- README.md
```

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker with Docker Compose

Docker is used for local PostgreSQL and for Testcontainers-backed persistence tests. The backend and frontend run directly on the host during local development.

## First-Time Setup

From the repository root:

```bash
npm install --prefix frontend
cp frontend/.env.example frontend/.env
docker compose up -d postgres
```

The root `dev:backend` script starts Spring Boot with the `local` profile, which reads local database settings from `backend/src/main/resources/application-local.yml`. Copy `backend/.env.example` only if your shell, IDE, or hosting workflow loads environment files explicitly.

## Run Locally

Start each process in its own terminal.

### 1. PostgreSQL

```bash
npm run dev:db
```

This starts the `postgres` service from `docker-compose.yml` on port `5432`.

Useful Docker commands:

```bash
docker compose ps
docker compose logs -f postgres
docker compose down
```

### 2. Spring Boot Backend

```bash
npm run dev:backend
```

Equivalent command:

```bash
cd backend
mvn spring-boot:run
```

The backend runs at `http://localhost:8080`.

Key URLs:

- API base URL: `http://localhost:8080/api`
- Health endpoint: `http://localhost:8080/actuator/health`

Liquibase runs automatically during backend startup before Hibernate validates the schema.

### 3. Frontend

```bash
npm run dev:frontend
```

Equivalent command:

```bash
cd frontend
npm run dev
```

The frontend runs at `http://localhost:5173` and calls the backend through `VITE_API_BASE_URL`.

## Database And Liquibase

Local PostgreSQL is provided by `docker-compose.yml`:

```text
Database: gaming-night
Username: gaming-night
Password: gaming-night
Port: 5432
```

The backend uses:

```text
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml
```

Liquibase applies changesets from:

```text
backend/src/main/resources/db/changelog/db.changelog-master.yaml
backend/src/main/resources/db/changelog/changes/
```

Fresh databases are automatically populated with development seed data:

- 12 players
- 2 users: `admin` / `admin` and `user` / `user`
- 2 predefined teams
- 2 games
- 1 setup-state competition
- 288 seeded team names for generated teams

After starting the backend and frontend, open `http://localhost:5173` and log in. Admin users can see every section. Regular users can see their own user page and competitions where their linked player belongs to a competition team.

To add a database change:

1. Add a new YAML changeset under `backend/src/main/resources/db/changelog/changes/`.
2. Use the next zero-padded prefix and a descriptive filename, for example `0018-add-avatar-to-players.yaml`.
3. Set the changeset `id` to the filename without `.yaml`.
4. Append the file to `db.changelog-master.yaml`.
5. Start the backend against local PostgreSQL and confirm Liquibase applies the change.

Do not edit a changeset that has already been applied to any shared or production database.

## Environment Variables

Production backend values are required by `backend/src/main/resources/application.yml` and must be supplied as real environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gaming-night
SPRING_DATASOURCE_USERNAME=gaming-night
SPRING_DATASOURCE_PASSWORD=gaming-night
APP_AUTH_TOKEN_SECRET=dev-only-change-me
CORS_ALLOWED_ORIGINS=http://localhost:5173
PORT=8080
```

Set `APP_AUTH_TOKEN_SECRET` to a strong private value outside local development. Existing login tokens become invalid when this value changes.

Local development uses `backend/src/main/resources/application-local.yml` through `npm run dev:backend`. `backend/.env.example` documents the expected variables for shells, IDEs, or hosts that load environment files explicitly.

Frontend defaults are defined in `frontend/.env.example`:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Tests And Builds

Run backend tests:

```bash
npm run test:backend
```

Run frontend tests:

```bash
npm run test:frontend
```

For CI/CD pipeline with GitHub Actions (automated testing on push/pull request), see [docs/github-actions.md](docs/github-actions.md).

Build backend:

```bash
npm run build:backend
```

Build frontend:

```bash
npm run build:frontend
```

Backend persistence tests use Testcontainers and require Docker. They are skipped automatically when Docker is unavailable.

## Documentation

- [docs/README.md](docs/README.md): Documentation index
- [docs/architecture.md](docs/architecture.md): Layers, domain concepts, feature flows, and API map
- [docs/testing.md](docs/testing.md): Test strategy and commands
- [docs/deployment.md](docs/deployment.md): Deployment, runtime configuration, and migration workflow
- [docs/ai-instructions.md](docs/ai-instructions.md): Coding assistant rules for this repository

## Deployment

See [docs/deployment.md](docs/deployment.md) for production setup.

Short version:

- Deploy the frontend as static assets from `frontend`, built with `npm run build`.
- Deploy the backend on Render as a Docker web service using `backend/Dockerfile`.
- Use Supabase PostgreSQL for production; for Render, use the Supabase session pooler JDBC URL unless direct IPv6 access or the IPv4 add-on is available.
- Provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_AUTH_TOKEN_SECRET`, and `CORS_ALLOWED_ORIGINS` as Render environment variables.
- Deploy the frontend to Cloudflare Pages with `VITE_API_BASE_URL` pointing at the Render backend `/api`.
- GitHub Actions tests, builds, runs dependency scanning, and can deploy automatically from `main` when the required GitHub secrets are configured.
- Keep Hibernate set to `ddl-auto=validate`; Liquibase owns schema changes.
