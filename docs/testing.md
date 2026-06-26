# Testing

## Backend

Run:

```bash
cd backend
mvn test
```

Test types included:

- **Domain tests** (`se.backede.domain.model`): validate model behavior (validation, state transitions) without Spring. Examples: `ItemTest`, `MatchTest`.
- **Use case tests** (`se.backede.application.usecase`): mock all repository ports and test workflow logic. Examples: `ItemUseCaseServiceTest`, `CompetitionRunUseCaseServiceTest`, `LeaderboardUseCaseServiceTest`.
- **Controller tests** (`se.backede.infrastructure.web`): use `@WebMvcTest` to verify HTTP behavior, request validation, error mapping, and response shape. Examples: `ItemControllerTest`, `CompetitionRunControllerTest`.
- **Persistence tests** (`se.backede.infrastructure.persistence`): use Testcontainers PostgreSQL to verify the JPA adapter and Liquibase schema. Requires Docker.

Repository tests are skipped automatically when Docker is not available (`@Testcontainers(disabledWithoutDocker = true)`).

## Frontend

Run:

```bash
cd frontend
npm test
```

Frontend tests use Vitest and React Testing Library. They focus on behavior visible to the user, such as navigation, form submission, validation errors, and empty states.

New tests added for Areas E and F:
- `GameStepNav.test.tsx`: verifies game navigation (disabled states, click handlers, rendering all game names).
- `GameTeamLeaderboard.test.tsx`: verifies leaderboard table renders column header, team names, values, and empty state.

## TDD Workflow

1. Write or update a failing test that describes the behavior.
2. Run the smallest relevant test command.
3. Implement the smallest clear change.
4. Refactor while tests stay green.
5. Add edge cases for validation, not-found, and error states.

Keep tests readable. Test behavior, not implementation details.

## Key Behaviors Covered

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
