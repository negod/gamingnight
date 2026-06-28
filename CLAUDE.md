# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Read [`docs/ai-instructions.md`](docs/ai-instructions.md) before changing code. It is the authoritative source for architecture rules, naming conventions, TDD workflow, feature workflow, and security requirements. What follows here is the operational quick-reference.

---

## Commands

All commands can be run from the repository root via the convenience scripts in the root `package.json`, or directly from each sub-project.

### Start development

```bash
npm run dev:db          # start PostgreSQL in Docker (must be first)
npm run dev:backend     # Spring Boot on http://localhost:8080
npm run dev:frontend    # Vite dev server on http://localhost:5173
```

### Tests

```bash
npm run test:backend    # all backend tests (cd backend && mvn test)
npm run test:frontend   # all frontend tests (cd frontend && npm test)
```

Run a single backend test class:
```bash
cd backend && mvn test -Dtest=CompetitionTest
```

Run a single backend test method:
```bash
cd backend && mvn test -Dtest=CompetitionTest#shouldRejectBlankName
```

Run a single frontend test file:
```bash
cd frontend && npx vitest run src/features/competitions/components/CompetitionForm.test.tsx
```

### Build

```bash
npm run build:backend   # cd backend && mvn clean package
npm run build:frontend  # cd frontend && npm run build
```

### First-time setup

```bash
npm install --prefix frontend
cp frontend/.env.example frontend/.env
docker compose up -d postgres
```

---

## Architecture

### Backend layers (Clean Architecture, dependencies point inward)

```
infrastructure  →  application  →  domain
```

| Layer | Package | Contains |
|---|---|---|
| `domain` | `se.backede.domain` | Pure Java records (domain models), repository port interfaces. Zero framework imports. |
| `application` | `se.backede.application` | Use-case services, DTOs, mappers between DTOs and domain models. |
| `infrastructure` | `se.backede.infrastructure` | Spring controllers, JPA entities, JPA adapters, Spring Data repos, config. |
| `shared` | `se.backede.shared` | Framework-free exceptions (`DomainValidationException`). |

Domain models are Java **records**. All validation (null checks, blank checks, length limits, range checks) lives in the compact constructor and throws `DomainValidationException`. No JPA annotations, no Spring annotations ever touch domain models.

Each domain concept has exactly this chain:
`DomainModel` → `DomainRepositoryPort` (interface in domain) → `JpaDomainRepositoryAdapter` (implements port) → `SpringDataDomainRepository` (JPA interface) + `DomainEntity` (JPA entity) + `DomainJpaMapper`.

### Competition lifecycle

A `Competition` moves through three states: **setup → started → finished**.

- **Setup**: teams and games are assigned; no matches exist yet.
- **Started**: `POST /api/competitions/{id}/start` generates round-robin matches; results can be entered via `PUT /api/competitions/{id}/matches/{mid}/results`; teams and games are locked.
- **Finished**: no further edits; leaderboards remain readable.

State transitions are guarded in the domain model (`Competition.start()`, `Competition.finish()`), not only in use-case services.

### Frontend structure

```
src/
  app/           # App shell, routing (App.tsx)
  pages/         # One file per route — thin orchestration only (load data, wire callbacks)
  features/      # Feature modules
    <feature>/
      api/        # Functions that call the backend (use shared apiClient)
      components/ # Focused UI components (receive data as props, no direct API calls)
      hooks/      # Custom hooks for data loading within a feature
  shared/
    api/          # apiClient.ts (single Axios/fetch wrapper), healthApi.ts
    components/   # Reusable cross-feature components
    types/        # TypeScript types mirroring backend DTOs
```

Pages load data and pass it down. Components are presentational. API calls live only in `features/<name>/api/` or `shared/api/`. Never call `fetch` or the API client directly from a reusable component.

### Key backend API endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET/POST/PUT/DELETE` | `/api/players` | Player CRUD |
| `GET/POST/PUT/DELETE` | `/api/teams` | Team CRUD |
| `GET/POST/PUT/DELETE` | `/api/games` | Game CRUD |
| `GET/POST/PUT/DELETE` | `/api/competitions` | Competition CRUD |
| `POST` | `/api/competitions/{id}/start` | Start competition, generate matches |
| `POST` | `/api/competitions/{id}/finish` | Finish competition, lock edits |
| `GET/PUT` | `/api/competitions/{id}/matches` | List matches / enter results |
| `GET` | `/api/competitions/{id}/leaderboard` | Per-game and total leaderboards |
| `GET/POST/PUT/DELETE` | `/api/users` | User management |
| `GET` | `/actuator/health` | Health check |

### Database migrations

All schema changes are Liquibase YAML changesets in `backend/src/main/resources/db/changelog/changes/`. The next prefix is `0011` (existing files have irregular numbering due to historical deletions — see `docs/remediation-plan.md` LIQ-1). Register every new file in `db.changelog-master.yaml`. Never edit an applied changeset.

---

## Adding a feature (summary)

Full workflow is in `docs/ai-instructions.md`. The order that matters:

1. Domain model + repository port → domain tests
2. DTOs + use-case service → use-case tests with mocked ports  
3. JPA entity + adapter + Spring Data repo → controller + persistence tests
4. Liquibase changeset
5. Frontend: `api/` functions → components → page → route in `App.tsx`
6. Documentation updates (`README.md`, `docs/architecture.md`, etc.)
