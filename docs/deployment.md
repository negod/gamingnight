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
APP_AUTH_TOKEN_SECRET=dev-only-change-me
CORS_ALLOWED_ORIGINS=http://localhost:5173
PORT=8080
```

For local development, `npm run dev:backend` activates the `local` profile and uses `backend/src/main/resources/application-local.yml` to connect to the Docker Compose PostgreSQL database.

**Important**: Outside the `local` profile, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` must always be explicitly supplied. The application will fail to start if these environment variables are not set, as there are no default values configured.

`APP_AUTH_TOKEN_SECRET` must also be explicitly supplied outside local development. The local profile sets `dev-only-change-me` only for development; non-local startup fails fast if the token secret is missing.

These values can come from exported shell variables, IDE run configuration, container configuration, or hosting-provider secrets. Spring Boot does not automatically load `backend/.env` unless the local tooling explicitly sources it.

Important backend settings from `application.yml`:

```text
app.auth.token-secret=${APP_AUTH_TOKEN_SECRET}
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
- 2 users: `admin` / `admin` and `user` / `user`
- 2 predefined teams
- 2 games
- 1 setup-state competition
- 288 generated-team name candidates

Change seeded development passwords before exposing a non-local environment. Use a strong `APP_AUTH_TOKEN_SECRET`; rotating this value invalidates existing login tokens.

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
    |-- 0009-create-team-names.yaml
    |-- 0010-create-users.yaml
    |-- 0011-add-user-passwords.yaml
    |-- 0012-add-email-to-users.yaml
    |-- 0013-add-game-rule-columns.yaml
    |-- 0014-migrate-game-seed-data.yaml
    |-- 0015-apply-game-rule-not-null.yaml
    |-- 0016-drop-old-game-columns.yaml
    `-- 0017-backfill-game-rule-columns.yaml
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
      id: 0018-add-avatar-to-players
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

The frontend is a static site. It must be rebuilt when `VITE_API_BASE_URL` changes. If GitHub Actions should be the production release gate, deploy Cloudflare Pages through Wrangler/direct upload from the workflow and do not also enable Cloudflare Pages Git auto-deploy for `main`.

Because the app uses React Router `BrowserRouter`, `frontend/public/_redirects` contains this Cloudflare Pages SPA fallback:

```text
/* /index.html 200
```

### Backend: Render Web Service

Configuration:

```text
Root directory: backend
Runtime: Docker
Dockerfile path: Dockerfile
Health check path: /actuator/health
```

Environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-REGION.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
APP_AUTH_TOKEN_SECRET=long-random-production-secret
CORS_ALLOWED_ORIGINS=https://your-frontend-domain
PORT=8080
```

Render provides `PORT` automatically for many services. The application reads it through `server.port=${PORT:8080}`.

For Supabase, use the session pooler connection string for the Render backend unless your service can reach the direct IPv6 database endpoint or the Supabase project has the IPv4 add-on. The session pooler is the right fit for a persistent backend on IPv4-only networks. The username usually has the form `postgres.PROJECT_REF`; copy the exact JDBC host, username, and database name from Supabase's connection panel.

Render should be configured with auto-deploy disabled if GitHub Actions must be the release gate. In that setup, GitHub Actions triggers Render through `RENDER_DEPLOY_HOOK_URL` only after tests, builds, and the dependency scan pass.

### GitHub Actions Deployment

The repository includes `.github/workflows/ci.yml`.

On every push or pull request to `main` or `develop`, it runs:

- backend tests,
- frontend tests,
- backend Docker image build,
- frontend production build,
- OWASP Dependency-Check.

On pushes to `main`, it also deploys when these GitHub secrets are configured:

```text
RENDER_DEPLOY_HOOK_URL
CLOUDFLARE_ACCOUNT_ID
CLOUDFLARE_API_TOKEN
CLOUDFLARE_PAGES_PROJECT_NAME
VITE_API_BASE_URL
```

The dependency scan reads `NVD_API_KEY` from GitHub Actions secrets, with a fallback to an Actions variable of the same name, and lets the OWASP Dependency-Check Maven plugin read it from the environment. The workflow caches the Dependency-Check data directory at `~/.cache/dependency-check`; the first run after a cache miss downloads the NVD database, while later runs reuse the cached database and fetch updates.

After the Render backend and Cloudflare frontend deployment jobs complete successfully on `main`, the workflow runs a production E2E job. Manual and scheduled E2E runs also require the backend tests, frontend tests, builds, and dependency scan to pass first. The E2E job requires these additional GitHub secrets:

```text
E2E_BASE_URL
E2E_API_BASE_URL
E2E_ADMIN_USERNAME
E2E_ADMIN_PASSWORD
E2E_USER_USERNAME
E2E_USER_PASSWORD
```

`E2E_BASE_URL` must be the deployed Cloudflare frontend origin. `E2E_API_BASE_URL` must be the deployed Render backend API base URL ending in `/api`; the workflow derives `/actuator/health` from it while waiting for the backend.

The E2E users must be dedicated production-safe accounts. Do not use the seeded development credentials in production. The admin account needs access to create and delete `e2e-` prefixed players, teams, games, and competitions. The normal user account needs access to its own user page and open competition registration.

Post-deploy E2E behavior:

- Pushes to `main` run `@smoke` tests and feature-tagged tests selected from changed files.
- Manual `workflow_dispatch` and the weekly schedule run the full Playwright suite.
- The job uploads the Playwright report, traces, screenshots, and videos as workflow artifacts.
- If any required E2E secret is missing, the E2E job fails instead of skipping the Playwright run.

See [github-actions.md](github-actions.md) for the full CI/CD setup.

## Production Checklist

- PostgreSQL is reachable from the backend.
- `SPRING_DATASOURCE_URL` uses the Supabase production database, preferably the session pooler URL for Render.
- `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` are set as secrets.
- `APP_AUTH_TOKEN_SECRET` is set to a strong private value and is not the development default.
- `CORS_ALLOWED_ORIGINS` is the exact frontend origin, not `*`.
- Frontend `VITE_API_BASE_URL` points to the deployed backend `/api`.
- Liquibase runs successfully during backend startup.
- Hibernate remains on `ddl-auto=validate`.
- Render backend is configured as Docker runtime using `backend/Dockerfile`.
- Cloudflare Pages receives the SPA `_redirects` fallback.
- GitHub Actions deploy secrets are configured before relying on automatic deploys.
- Dedicated production E2E users and E2E GitHub secrets are configured before relying on post-deploy E2E checks.
- Real `.env` files are not committed.
