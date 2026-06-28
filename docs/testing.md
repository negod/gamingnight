# Testing

This document describes the test strategy, commands, coverage areas, and Docker requirements for Gaming Night. For an overview of all documentation, see [README.md](README.md).

## Table of Contents

- [Backend Testing](#backend-testing)
- [Frontend Testing](#frontend-testing)
- [TDD Workflow](#tdd-workflow)
- [Key Behaviors Covered](#key-behaviors-covered)

## Backend Testing

Run all backend tests from the repository root:

```bash
npm run test:backend
```

Equivalent command:

```bash
cd backend
mvn test
```

### Test Types

The backend uses a layered testing approach:

- **Domain Tests** (`se.backede.domain.model`): Validate model behavior and state transitions without Spring dependencies. Examples: `PlayerTest`, `GameTest`, `CompetitionTest`, `MatchTest`.
- **Use Case Tests** (`se.backede.application.usecase`): Test workflow logic with mocked repository ports. Examples: `PlayerUseCaseServiceTest`, `GameUseCaseServiceTest`, `CompetitionUseCaseServiceTest`, `CompetitionRunUseCaseServiceTest`, `LeaderboardUseCaseServiceTest`.
- **Controller Tests** (`se.backede.infrastructure.web`): Use `@WebMvcTest` to verify HTTP behavior, request validation, error mapping, and response shape. Examples: `PlayerControllerTest`, `GameControllerTest`, `CompetitionControllerTest`, `CompetitionRunControllerTest`.
- **Persistence Tests** (`se.backede.infrastructure.persistence`): Use Testcontainers PostgreSQL to verify JPA adapters and Liquibase schema. Requires Docker.

**Note**: Repository tests are skipped automatically when Docker is not available (`@Testcontainers(disabledWithoutDocker = true)`).

For persistence tests, make sure Docker is running before executing `mvn test`. The local `postgres` container from `docker-compose.yml` is not used by Testcontainers; Testcontainers starts its own isolated PostgreSQL container.

## Frontend Testing

Run frontend tests from the repository root:

```bash
npm run test:frontend
```

Equivalent command:

```bash
cd frontend
npm test
```

Frontend tests use Vitest and React Testing Library. They focus on behavior visible to the user, such as navigation, form submission, validation errors, and empty states.

New tests added for Area A:
- `PlayerForm.test.tsx`: verifies player form submission and required-name validation.
- `PlayerList.test.tsx`: verifies empty state, edit links, and delete action.

Tests for Area D:
- `CompetitionForm.test.tsx`: verifies setup form submission, game order, and team selection.
- `CompetitionList.test.tsx`: verifies empty state, setup/started status, edit visibility, and delete action.
- `GenerateTeamsWizard.test.tsx`: verifies player selection, team size, leftover summary, and validation.

Tests for Areas E and F:
- `GameStepNav.test.tsx`: verifies game navigation (disabled states, click handlers, rendering all game names).
- `GameTeamLeaderboard.test.tsx`: verifies leaderboard table renders column header, team names, values, and empty state.
- `GamePlayerLeaderboard.test.tsx`: verifies per-game player leaderboard rendering.
- `TotalTeamLeaderboard.test.tsx`: verifies total team leaderboard rendering.
- `TotalPlayerLeaderboard.test.tsx`: verifies total player leaderboard rendering.

## Build Verification

Run backend build:

```bash
npm run build:backend
```

Run frontend build:

```bash
npm run build:frontend
```

Use build commands before deployment because they catch packaging and TypeScript issues that unit tests may not cover.

## TDD Workflow

1. Write or update a failing test that describes the behavior.
2. Run the smallest relevant test command.
3. Implement the smallest clear change.
4. Refactor while tests stay green.
5. Add edge cases for validation, not-found, and error states.

Keep tests readable. Test behavior, not implementation details.

## Key Behaviors Covered

### Player Management (Area A)

| Scenario | Test |
|---|---|
| Player names are trimmed and validated | `PlayerTest` |
| Players can be created, listed newest-first, fetched, updated, and deleted | `PlayerUseCaseServiceTest` |
| `/api/players` returns correct status codes for CRUD, validation, and not-found cases | `PlayerControllerTest` |
| Player UI supports validation, edit links, delete action, and empty state | `PlayerForm.test.tsx`, `PlayerList.test.tsx` |

### Game Management (Area B)

| Scenario | Test |
|---|---|
| Valid game is created with all fields | `GameTest.createsGameWithAllFields` |
| Null description is treated as empty | `GameTest.treatsNullDescriptionAsEmpty` |
| Blank name is rejected | `GameTest.rejectsMissingName` |
| Name over 120 characters is rejected | `GameTest.rejectsTooLongName` |
| Update changes fields and updatedAt | `GameTest.updatesMutableFieldsAndUpdatedAt` |
| Game CRUD use-case operations succeed and throw not-found on unknown id | `GameUseCaseServiceTest` |
| `/api/games` returns correct status codes for CRUD, validation, and not-found cases | `GameControllerTest` |

### Team Management (Area C)

| Scenario | Test |
|---|---|
| Manual team create and update reject duplicate names | `TeamUseCaseServiceTest` |

Current gap: team domain validation and `/api/teams` controller behavior should be covered by focused tests when team management receives more changes.

### Competition Setup (Area D)

| Scenario | Test |
|---|---|
| Competitions validate and update setup fields | `CompetitionTest` |
| Competitions can be created, listed, updated before start, deleted, and auto-generated with teams | `CompetitionUseCaseServiceTest` |
| Setup validates referenced games, teams, and players | `CompetitionUseCaseServiceTest` |
| Generated teams use unused seeded team names, and manual teams reject duplicate names | `CompetitionUseCaseServiceTest`, `TeamUseCaseServiceTest` |
| Started competitions cannot be edited or regenerated | `CompetitionUseCaseServiceTest`, `CompetitionControllerTest` |
| `/api/competitions` returns correct status codes for CRUD, validation, not-found, and generate-teams cases | `CompetitionControllerTest` |
| Competition setup UI supports game ordering, team selection, status display, delete action, and generate-team wizard | `CompetitionForm.test.tsx`, `CompetitionList.test.tsx`, `GenerateTeamsWizard.test.tsx` |

### Competition Run (Area E)

| Scenario | Test |
|---|---|
| Round-robin generates N*(N-1)/2 matches per game | `CompetitionRunUseCaseServiceTest.startGeneratesRoundRobinMatchesPerGame` |
| Double matches when `singleMatch = false` | `CompetitionRunUseCaseServiceTest.startDoubleMatchesWhenNotSingleMatch` |
| Competition marked `started = true` | `CompetitionRunUseCaseServiceTest.startMarksCompetitionAsStarted` |
| Starting an already-started competition throws | `CompetitionRunUseCaseServiceTest.startThrowsWhenAlreadyStarted` |
| Starting with fewer than 2 teams throws | `CompetitionRunUseCaseServiceTest.startThrowsWhenFewerThanTwoTeams` |
| Entering results replaces existing results | `CompetitionRunUseCaseServiceTest.enterResultsUpdatesMatch` |

### Leaderboards (Area F)

| Scenario | Test |
|---|---|
| Score-based game: highest first | `LeaderboardUseCaseServiceTest.gameTeamLeaderboardRanksHighestFirstForScoreBased` |
| Time-based game: lowest first | `LeaderboardUseCaseServiceTest.gameTeamLeaderboardRanksLowestFirstForTimeBased` |
| Average calculation method shows correct header | `LeaderboardUseCaseServiceTest.gameTeamLeaderboardAverageHeader` |
| Placement points applied and summed in total leaderboard | `LeaderboardUseCaseServiceTest.totalTeamLeaderboardAppliesPlacementPoints` |
| Tied teams receive the same rank | `LeaderboardUseCaseServiceTest.tiedTeamsGetSameRank` |
