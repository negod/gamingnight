# Gaming Night Documentation

This directory contains the technical documentation for Gaming Night.

Start with the main [README](../README.md) for a project overview, prerequisites, local setup, and common run commands.

## Documents

| Document | Purpose | Audience |
|---|---|---|
| [Main README](../README.md) | Project overview, local setup, run commands, environment variables | Everyone |
| [Architecture](architecture.md) | Backend layers, dependency rules, domain models, feature flows, API map, frontend structure | Developers |
| [Testing](testing.md) | Test commands, test strategy, Docker/Testcontainers notes, covered behavior | Developers |
| [GitHub Actions CI/CD](github-actions.md) | CI/CD pipeline setup, backend/frontend tests, e2e testing, security scanning | Developers, operators |
| [Deployment And Runtime Operations](deployment.md) | Local runtime, production deployment, database setup, Liquibase workflow | Developers, operators |
| [AI Instructions](ai-instructions.md) | Repository rules for AI coding assistants | AI assistants and maintainers |

## Documentation Responsibilities

Update documentation in the same change as code when the change affects behavior, setup, architecture, tests, deployment, or data models.

- Update [../README.md](../README.md) for user-facing behavior, setup commands, environment variables, or local run instructions.
- Update [architecture.md](architecture.md) for layer boundaries, domain concepts, feature flows, API changes, or frontend structure.
- Update [testing.md](testing.md) for test commands, fixtures, coverage expectations, or test strategy changes.
- Update [github-actions.md](github-actions.md) for CI/CD pipeline, GitHub Actions workflow, or testing infrastructure changes.
- Update [deployment.md](deployment.md) for runtime configuration, Docker, database, Liquibase, build, or release behavior.
- Update [ai-instructions.md](ai-instructions.md) when assistant workflow rules or repository conventions change.

## Local Operations Quick Links

- Setup and run locally: [../README.md#first-time-setup](../README.md#first-time-setup)
- Docker and PostgreSQL: [deployment.md#local-runtime](deployment.md#local-runtime)
- Spring Boot backend: [deployment.md#backend-runtime](deployment.md#backend-runtime)
- Frontend: [deployment.md#frontend-runtime](deployment.md#frontend-runtime)
- Liquibase: [deployment.md#liquibase-migration-workflow](deployment.md#liquibase-migration-workflow)
- Tests: [testing.md](testing.md)
