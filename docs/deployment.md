# Deployment And Runtime Operations

This document covers how Gaming Night is configured and run outside tests, including local Docker services, backend startup, frontend builds, production deployment, and Liquibase migrations.

For the project overview and quick start, see [../README.md](../README.md).

## Contents

- [Local Runtime](#local-runtime)
- [Backend Runtime](#backend-runtime)
- [Frontend Runtime](#frontend-runtime)
- [Database](#database)
- [Liquibase Migration Workflow](#liquibase-migration-workflow)
- [Production Deployment](#production-deployment)
- [Production Checklist](#production-checklist)

## Local Runtime

Local development uses three processes:

| Process | Command | URL |
|---|---|---|
| PostgreSQL | `npm run dev:db` | `localhost:5432` |
| Spring Boot API | `npm run dev:backend` | `http://localhost:8080` |
| Vite frontend | `npm run dev:frontend` | `http://localhost:5173` |

`docker-compose.yml` only runs PostgreSQL. The backend and frontend run directly on the host so code changes are fast and easy to inspect.

Useful Docker commands:

```bash
docker compose up -d postgres
docker compose ps
docker compose logs -f postgres
docker compose down
```

Use `docker compose down -v` only when you intentionally want to delete the local database volume and recreate the database from scratch.

## Backend Runtime

Run from the repository root:

```bash
npm run dev:backend
```

Equivalent command:

```bash
cd backend
mvn spring-boot:run
```

Build a production jar:

```bash
cd backend
mvn clean package
```

Run the jar:

```bash
java -jar backend/target/gaming-night-0.0.1-SNAPSHOT.jar
```

Required backend configuration:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gaming-night
SPRING_DATASOURCE_USERNAME=gaming-night
SPRING_DATASOURCE_PASSWORD=gaming-night
CORS_ALLOWED_ORIGINS=http://localhost:5173
PORT=8080
```

These values can come from exported shell variables, IDE run configuration, container configuration, or hosting-provider secrets. Spring Boot does not automatically load `backend/.env` unless the local tooling explicitly sources it.

Important backend settings from `application.yml`:

```text
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml
spring.jpa.open-in-view=false
```

Startup order:

1. Spring Boot starts.
2. Liquibase applies unapplied changesets.
3. Hibernate validates the schema.
4. Controllers become available.

## Frontend Runtime

Install dependencies:

```bash
npm install --prefix frontend
```

Run locally:

```bash
npm run dev:frontend
```

Build static assets:

```bash
npm run build:frontend
```

Preview the production build locally:

```bash
cd frontend
npm run preview
```

Frontend configuration:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

For production, set `VITE_API_BASE_URL` to the deployed backend API base URL before building the frontend.

## Database

Local PostgreSQL is defined in `docker-compose.yml`:

```text
Image: postgres:16-alpine
Container: gaming-night-postgres
Database: gaming-night
Username: gaming-night
Password: gaming-night
Host port: 5432
```

Production can use any PostgreSQL-compatible provider, for example Supabase, Neon, Render PostgreSQL, or a self-managed PostgreSQL instance.

Provider JDBC URL format:

```text
jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
```

Use `sslmode=require` unless the provider recommends a different SSL setting.

Fresh databases are seeded by Liquibase with:

- 12 players
- 2 predefined teams
- 2 games
- 1 setup-state competition
- 288 generated-team name candidates

## Liquibase Migration Workflow

Schema and seed data changes are managed by Liquibase YAML changelogs.

Current layout:

```text
backend/src/main/resources/db/changelog/
|-- db.changelog-master.yaml
`-- changes/
    |-- 0002-create-players.yaml
    |-- 0002-create-teams.yaml
    |-- 0003-create-competitions.yaml
    |-- 0003-create-games.yaml
    |-- 0006-create-matches.yaml
    |-- 0007-create-player-results.yaml
    |-- 0008-seed-test-data.yaml
    `-- 0009-create-team-names.yaml
```

Add a migration:

1. Create a file in `changes/` with the next zero-padded prefix and a short description.
2. Set the changeset `id` to the filename without `.yaml`.
3. Use `author: backede` unless there is a clear reason to use another author.
4. Append the file to `db.changelog-master.yaml`.
5. Start local PostgreSQL with `docker compose up -d postgres`.
6. Start the backend and verify the migration applies.
7. Add or update tests when the schema change affects behavior.

Example:

```yaml
databaseChangeLog:
  - changeSet:
      id: 0010-add-avatar-to-players
      author: backede
      changes:
        - addColumn:
            tableName: players
            columns:
              - column:
                  name: avatar_url
                  type: varchar(500)
```

Rules:

- Never edit a changeset that has already been applied to any shared or production database.
- Do not use Hibernate `ddl-auto=update` or `ddl-auto=create`.
- Keep changelogs deterministic and environment-independent.
- Prefer additive migrations for production safety.
- Add rollback blocks for destructive or high-risk changes when rollback support is required.

## Production Deployment

### Frontend: Cloudflare Pages

Configuration:

```text
Project root: frontend
Build command: npm run build
Build output directory: dist
Environment: VITE_API_BASE_URL=https://your-backend-service/api
```

The frontend is a static site. It must be rebuilt when `VITE_API_BASE_URL` changes.

### Backend: Render Web Service

Configuration:

```text
Root directory: backend
Runtime: Java
Build command: mvn clean package
Start command: java -jar target/gaming-night-0.0.1-SNAPSHOT.jar
```

Environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
CORS_ALLOWED_ORIGINS=https://your-frontend-domain
PORT=8080
```

Render provides `PORT` automatically for many services. The application reads it through `server.port=${PORT:8080}`.

## Production Checklist

- PostgreSQL is reachable from the backend.
- `SPRING_DATASOURCE_URL` uses the provider's production database.
- `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` are set as secrets.
- `CORS_ALLOWED_ORIGINS` is the exact frontend origin, not `*`.
- Frontend `VITE_API_BASE_URL` points to the deployed backend `/api`.
- Liquibase runs successfully during backend startup.
- Hibernate remains on `ddl-auto=validate`.
- Real `.env` files are not committed.
