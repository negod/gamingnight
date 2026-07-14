# GitHub Actions CI/CD

This repository uses `.github/workflows/ci.yml` to test, build, and deploy Gaming Night.

## Pipeline

The workflow runs on pushes and pull requests to `main` and `develop`.

| Job | Purpose |
|---|---|
| `backend-test` | Runs the Spring Boot/JUnit test suite with Java 21. |
| `frontend-test` | Runs Vitest/React Testing Library tests with Node.js 20. |
| `backend-docker-build` | Builds the backend Docker image from `backend/Dockerfile`. |
| `frontend-build` | Builds the Vite static frontend and uploads `frontend/dist` as an artifact. |
| `dependency-check` | Runs the OWASP Dependency-Check Maven plugin. |
| `deploy-backend` | On `main` pushes, triggers Render through a deploy hook. |
| `deploy-frontend` | On `main` pushes, builds with the production API URL and deploys to Cloudflare Pages with Wrangler. |

Deploy jobs are safe to merge before secrets are configured. If a required deploy secret is missing, the job logs a warning and skips that deploy.

## Required GitHub Secrets

Add these in GitHub: `Settings -> Secrets and variables -> Actions -> Repository secrets`.

| Secret | Used by | Description |
|---|---|---|
| `RENDER_DEPLOY_HOOK_URL` | Backend deploy | Render deploy hook URL for the backend service. Disable Render auto-deploy if GitHub Actions should be the release gate. |
| `CLOUDFLARE_ACCOUNT_ID` | Frontend deploy | Cloudflare account id. |
| `CLOUDFLARE_API_TOKEN` | Frontend deploy | Cloudflare API token with permission to deploy the Pages project. |
| `CLOUDFLARE_PAGES_PROJECT_NAME` | Frontend deploy | Cloudflare Pages project name for Wrangler/direct upload deploys. |
| `VITE_API_BASE_URL` | Frontend deploy | Production backend API URL, for example `https://gaming-night-api.onrender.com/api`. |

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
2. GitHub Actions runs backend tests, frontend tests, backend Docker build, frontend build, and dependency scan.
3. If all required jobs pass, `deploy-backend` triggers Render.
4. If Cloudflare secrets are configured, `deploy-frontend` builds with `VITE_API_BASE_URL` and uploads `frontend/dist` to Cloudflare Pages.
5. Render starts the backend container, Liquibase applies pending migrations, and `/actuator/health` becomes available.

## Troubleshooting

| Symptom | Check |
|---|---|
| Backend deploy skipped | `RENDER_DEPLOY_HOOK_URL` exists in GitHub Actions secrets. |
| Frontend deploy skipped | All Cloudflare secrets and `VITE_API_BASE_URL` exist in GitHub Actions secrets. |
| Backend cannot start on Render | Render env vars include Supabase JDBC URL, username, password, and `APP_AUTH_TOKEN_SECRET`. |
| Browser gets CORS errors | Render `CORS_ALLOWED_ORIGINS` exactly matches the Cloudflare Pages production origin. |
| Direct route refresh returns 404 | Confirm `frontend/public/_redirects` is included in the Cloudflare Pages build output. |
| Liquibase fails | Check the Supabase database user has permission to create tables/indexes and insert seed data. |

## Notes

- Do not commit real `.env` files or secrets.
- Keep Render auto-deploy disabled if GitHub Actions must be the only production release gate.
- The frontend build embeds `VITE_API_BASE_URL` at build time, so changing the backend URL requires a new frontend deploy.
