# Codex Instructions

Read and follow [`docs/ai-instructions.md`](docs/ai-instructions.md) before changing code in this repository. That file is the canonical source for architecture rules, naming conventions, TDD workflow, feature placement, security expectations, and documentation requirements.

This file is the Codex operational quick-reference. Keep it focused on practical information that is not already covered by the canonical instructions.

## Codex Workflow Notes

- Start by checking the current context with `git status --short` and targeted file reads. The worktree may already contain user or generated changes; do not revert unrelated files.
- Prefer `rg` and `rg --files` for discovery.
- Use `apply_patch` for manual edits.
- Keep changes scoped to the requested behavior, and update docs in the same turn when behavior, setup, schema, or API contracts change.
- When adding database migrations, inspect `backend/src/main/resources/db/changelog/changes/` and `db.changelog-master.yaml` first. The current next prefix is `0012`.

## Commands

Run from the repository root unless noted.

```bash
npm run dev:db          # PostgreSQL via docker compose
npm run dev:backend     # Spring Boot API on http://localhost:8080
npm run dev:frontend    # Vite frontend on http://localhost:5173
```

```bash
npm run test:backend    # cd backend && mvn test
npm run test:frontend   # cd frontend && npm test
npm run build:backend   # cd backend && mvn clean package
npm run build:frontend  # cd frontend && npm run build
```

Focused commands:

```bash
cd backend && mvn test -Dtest=CompetitionUseCaseServiceTest
cd backend && mvn test -Dtest=CompetitionUseCaseServiceTest#methodName
cd frontend && npx vitest run src/features/competitions/components/CompetitionForm.test.tsx
```

## Local Runtime Facts

- Docker Compose only runs PostgreSQL; backend and frontend run on the host.
- Backend API base URL: `http://localhost:8080/api`.
- Frontend dev URL: `http://localhost:5173`.
- Health endpoint: `http://localhost:8080/actuator/health`.
- Liquibase runs automatically on backend startup before Hibernate validates the schema.
- Testcontainers persistence tests require Docker. In environments without Docker, those tests are skipped by the test annotations.

## Authentication Seed Data

Fresh local databases are seeded with development logins:

```text
admin / admin
user / user
```

Admin users can access all tabs and API operations. Regular users can access their own user page and competitions where their linked player belongs to a competition team.

Set `APP_AUTH_TOKEN_SECRET` to a strong private value outside local development.

## Frontend Placement Reminders

- Route screens live in `frontend/src/pages`.
- Feature API functions live in `frontend/src/features/<feature>/api`.
- Feature components live in `frontend/src/features/<feature>/components`.
- Shared cross-feature helpers live under `frontend/src/shared`.
- Reusable components should not call the API directly.

## Useful Documentation

- `README.md`: setup, run commands, environment variables.
- `docs/architecture.md`: domain concepts, feature flows, API map.
- `docs/testing.md`: test strategy and coverage expectations.
- `docs/deployment.md`: runtime configuration, Docker/PostgreSQL, Liquibase, production notes.
- `docs/remediation-plan.md`: known cleanup/security follow-ups; verify current code before treating an item as still open.
