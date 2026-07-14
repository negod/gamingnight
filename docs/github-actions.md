# GitHub Actions CI/CD

This repository uses `.github/workflows/ci.yml` to test, build, and deploy Gaming Night.

## Pipeline

The workflow runs tests and builds on pushes and pull requests to `main` and `develop`. The OWASP Dependency-Check security scan runs on the weekly schedule and when the workflow is started manually.

| Job | Purpose |
|---|---|
| `backend-test` | Runs the Spring Boot/JUnit test suite with Java 21. |
| `frontend-test` | Runs Vitest/React Testing Library tests with Node.js 20. |
| `backend-docker-build` | Builds the backend Docker image from `backend/Dockerfile`. |
| `frontend-build` | Builds the Vite static frontend and uploads `frontend/dist` as an artifact. |
| `dependency-check` | Runs the OWASP Dependency-Check Maven plugin on scheduled/manual workflow runs, caches its vulnerability database, and uploads the HTML report. |
| `deploy-backend` | On `main` pushes, triggers Render through a deploy hook. |
| `deploy-frontend` | On `main` pushes, builds with the production API URL and deploys to Cloudflare Pages with Wrangler. |

Deploy jobs only run on pushes to `main`. When they run, missing required deploy secrets fail the job instead of skipping it so release failures stay visible.

## Required GitHub Secrets

Add these in GitHub: `Settings -> Secrets and variables -> Actions -> Repository secrets`.

| Secret | Used by | Description |
|---|---|---|
| `RENDER_DEPLOY_HOOK_URL` | Backend deploy | Render deploy hook URL for the backend service. Disable Render auto-deploy if GitHub Actions should be the release gate. |
| `CLOUDFLARE_ACCOUNT_ID` | Frontend deploy | Cloudflare account id. |
| `CLOUDFLARE_API_TOKEN` | Frontend deploy | Cloudflare API token with permission to deploy the Pages project. |
| `CLOUDFLARE_PAGES_PROJECT_NAME` | Frontend deploy | Cloudflare Pages project name for Wrangler/direct upload deploys. |
| `VITE_API_BASE_URL` | Frontend deploy | Production backend API URL, for example `https://gaming-night-api.onrender.com/api`. |
| `NVD_API_KEY` | Dependency scan | Optional but recommended NVD API key for OWASP Dependency-Check. Store it as a repository secret; the workflow also accepts an Actions variable with the same name. The key is read from the environment by the Maven plugin. |

Render runtime secrets are configured in Render, not GitHub, unless you later change the workflow to call the Render API directly.

## Render Backend Service

Create the backend as a Render Web Service backed by the GitHub repository.

Recommended settings:

```text
Runtime: Docker
Root directory: backend
Dockerfile path: Dockerfile
Health check path: /actuator/health
Auto-deploy: off, if GitHub Actions should gate production deploys
```

Set these Render environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-REGION.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.PROJECT_REF
SPRING_DATASOURCE_PASSWORD=your-supabase-database-password
APP_AUTH_TOKEN_SECRET=long-random-production-secret
CORS_ALLOWED_ORIGINS=https://your-cloudflare-pages-domain
```

Render supplies `PORT`; the backend reads it through `server.port=${PORT:8080}`.

## Cloudflare Pages Frontend

The workflow deploys with Wrangler/direct upload:

```bash
npx wrangler@latest pages deploy frontend/dist --project-name "$CLOUDFLARE_PAGES_PROJECT_NAME"
```

Use this CI-managed deploy path if GitHub Actions should be the production release gate. Do not also enable Cloudflare Pages Git auto-deploy for the same production branch, because that can publish before the GitHub Actions pipeline finishes.

`frontend/public/_redirects` contains the SPA fallback:

```text
/* /index.html 200
```

This is required because the frontend uses `BrowserRouter`; direct visits to routes like `/competitions` must serve `index.html`.

## Supabase PostgreSQL

Use the Supabase session pooler for the Render backend unless your Render service can reach Supabase directly over IPv6 or your Supabase project has the IPv4 add-on.

Use the JDBC form of the Supabase session pooler URL:

```text
jdbc:postgresql://aws-REGION.pooler.supabase.com:5432/postgres?sslmode=require
```

Keep Liquibase enabled. On first backend startup against an empty Supabase database, Liquibase creates the schema and seeds the development data.

## Release Flow

1. Push or merge to `main`.
2. GitHub Actions runs backend tests, frontend tests, backend Docker build, and frontend build.
3. If all required jobs pass, `deploy-backend` triggers Render.
4. `deploy-frontend` builds with `VITE_API_BASE_URL` and uploads `frontend/dist` to Cloudflare Pages.
5. Render starts the backend container, Liquibase applies pending migrations, and `/actuator/health` becomes available.

## Troubleshooting

| Symptom | Check |
|---|---|
| Backend deploy fails because configuration is missing | `RENDER_DEPLOY_HOOK_URL` exists in GitHub Actions secrets. |
| Frontend deploy fails because configuration is missing | All Cloudflare secrets and `VITE_API_BASE_URL` exist in GitHub Actions secrets. |
| Dependency scan warns about missing NVD API key | Add `NVD_API_KEY` under GitHub Actions repository secrets, or as an Actions variable if you already manage it there. |
| Dependency scan downloads the full NVD database | The first run after a cache miss is expected to download the database. The workflow restores `~/.cache/dependency-check`, runs `dependency-check:update-only`, saves the refreshed database before scanning, then scans with `autoUpdate=false`. The cache key rotates daily so a failed vulnerability scan does not discard the downloaded database. |
| Dependency scan reports suppression or OSS Index warnings | The Maven plugin reads suppressions from `backend/owasp-suppressions.xml` using the backend project base directory. Sonatype OSS Index is disabled; NVD remains the authoritative vulnerability source for the CI scan. |
| Dependency scan fails | Scheduled/manual workflow runs become red and include the uploaded `dependency-check-report` artifact. Push deploys are not blocked by the scan. |
| Backend cannot start on Render | Render env vars include Supabase JDBC URL, username, password, and `APP_AUTH_TOKEN_SECRET`. |
| Browser gets CORS errors | Render `CORS_ALLOWED_ORIGINS` exactly matches the Cloudflare Pages production origin. |
| Login returns 405 in the browser | Confirm the frontend was rebuilt with `VITE_API_BASE_URL` pointing to the backend origin ending in `/api`, for example `https://gaming-night-api.onrender.com/api`. A 405 on `/auth/login` usually means the static frontend host received the POST instead of the backend. |
| Direct route refresh returns 404 | Confirm `frontend/public/_redirects` is included in the Cloudflare Pages build output. |
| Liquibase fails | Check the Supabase database user has permission to create tables/indexes and insert seed data. |

## Notes

- Do not commit real `.env` files or secrets.
- Keep Render auto-deploy disabled if GitHub Actions must be the only production release gate.
- The frontend build embeds `VITE_API_BASE_URL` at build time, so changing the backend URL requires a new frontend deploy.
