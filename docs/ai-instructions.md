# AI Coding Assistant Instructions

This project is designed to be easy to continue with Codex, Claude, Mistral, and similar coding assistants.

## Default Assistant Entrypoints

The canonical instructions are in this file. Root-level assistant entrypoints must point here:

- `AGENTS.md` for Codex.
- `CLAUDE.md` for Claude.
- `MISTRAL.md` for Mistral-compatible agents.

When changing these instructions, update this file first and keep the root-level entrypoints as short delegates to avoid duplicated rules drifting apart.

## Architecture Rules

- Keep backend dependencies pointing inward: `infrastructure -> application -> domain`.
- Do not import Spring, JPA, servlet, or database packages in `domain`.
- Do not return JPA entities from use cases or controllers.
- Do not put business logic in controllers.
- Do not call repositories directly from controllers.
- Use domain models for business rules.
- Use DTOs for request and response shapes.
- Use mappers between DTOs, domain models, and persistence entities.

## Naming Conventions

- Domain model: `Item`
- Repository port: `ItemRepositoryPort`
- Use case service: `ItemUseCaseService`
- Request DTO: `CreateItemRequest`, `UpdateItemRequest`
- Response DTO: `ItemResponse`
- Persistence entity: `ItemEntity`
- JPA adapter: `JpaItemRepositoryAdapter`
- Spring Data repository: `SpringDataItemRepository`

## Adding a New Feature

1. Add domain model and repository port under `backend/src/main/java/se/backede/domain`.
2. Write domain tests first.
3. Add application DTOs and use-case service under `application`.
4. Write use-case tests with mocked ports.
5. Add infrastructure adapters under `infrastructure`.
6. Write controller tests and persistence tests where useful.
7. Add frontend API functions under `frontend/src/features/<feature>/api`.
8. Add focused UI components under `frontend/src/features/<feature>/components`.
9. Add route pages under `frontend/src/pages`.
10. Add or update docs when architecture or setup changes.

## Writing Tests First

- Start with the behavior and expected outcome.
- Include invalid input and not-found scenarios.
- Use JUnit 5, AssertJ, and Mockito for backend unit tests.
- Use `@WebMvcTest` for controller behavior.
- Use Testcontainers for persistence behavior that depends on PostgreSQL.
- Use Vitest and React Testing Library for frontend behavior.

## Where Code Should Live

- Business rules: `domain/model` or `domain/service`.
- Repository interfaces: `domain/repository`.
- Use-case orchestration: `application/usecase`.
- DTOs: `application/dto`.
- DTO mappers: `application/mapper`.
- Controllers: `infrastructure/web`.
- JPA entities and adapters: `infrastructure/persistence`.
- Configuration: `infrastructure/config`.
- Shared framework-free exceptions: `shared/exception`.

## What Must Not Be Done

- Do not add JPA annotations to domain models.
- Do not inject Spring services into domain objects.
- Do not expose database entities through REST.
- Do not call `fetch` directly inside reusable UI components.
- Do not skip tests for validation and not-found behavior.
- Do not use Hibernate `ddl-auto=update` for production schema changes.

## Avoiding Clean Architecture Breakage

Before committing a backend change, inspect imports in `domain` and `application`.

`domain` should only depend on Java standard library and framework-free shared code.

`application` should not depend on persistence or web packages.

If a new dependency points outward, introduce a port in the domain or application layer and implement it in infrastructure.
