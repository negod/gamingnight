# Architecture

## Backend Layers

```text
infrastructure -> application -> domain
```

The domain is the center of the application. Domain models have no Spring, JPA, database, or HTTP annotations.

The application layer contains use cases and DTOs. Use case services own the user-facing workflows and coordinate validation, persistence, and mapping.

The infrastructure layer contains adapters:

- Controllers map HTTP requests to use cases.
- `Jpa*RepositoryAdapter` classes implement domain repository ports.
- Entity classes are the persistence models and are separate from domain models.
- `GlobalExceptionHandler` converts application exceptions into HTTP responses.

## Dependency Rules

- Domain must not import Spring, JPA, web, or database packages.
- Application may depend on domain and framework-free shared code.
- Infrastructure may depend on application, domain, and Spring.
- Controllers must not contain business logic.
- Persistence entities must not be returned from use cases or controllers.

## Domain Models

| Model | Description |
|---|---|
| `Item` | Example item (title, description) |
| `Player` | A registered participant |
| `Game` | A game with `GameType` (SCORE_BASED / TIME_BASED) and `CalculationMethod` (SUM / AVERAGE) |
| `Team` | A named group of players |
| `Competition` | An event with ordered games and teams; can be started once |
| `Match` | A single matchup between two teams within a game of a competition |
| `PlayerResult` | A player's individual score or time in a match (value type, no identity) |

## Feature Flows

### Creating and running a competition (Areas D + E)

1. Admin creates a competition via `POST /api/competitions` with game and team IDs.
2. Admin calls `POST /api/competitions/{id}/start`.
3. `CompetitionRunUseCaseService.start()` generates round-robin matches for every (game, team-pair) combination.
4. The competition is marked `started = true`; this cannot be reversed.
5. Admin enters results via `PUT /api/competitions/{cid}/matches/{mid}/results`.
6. Results are stored as `PlayerResult` entries attached to the `Match`.

### Leaderboards (Area F)

`LeaderboardUseCaseService` computes four views:

- **Per-game team** — aggregate player results per team using the game's `CalculationMethod`; rank using `GameType`.
- **Per-game player** — same logic per individual player.
- **Total team** — apply placement points (1st=100, 2nd=90, … 10th=10) per game, sum across all games.
- **Total player** — same placement-point system per player.

The column header (`Total Score`, `Average Score`, `Total Time`, `Average Time`) is computed from the game's type and calculation method and returned alongside the leaderboard rows.

## REST API

### Competition Run

| Method | Path | Description |
|---|---|---|
| POST | `/api/competitions/{id}/start` | Start competition, generate matches |
| GET | `/api/competitions/{cid}/games/{gid}/matches` | List matches for a game |
| PUT | `/api/competitions/{cid}/matches/{mid}/results` | Enter or update player results |

### Leaderboards

| Method | Path | Description |
|---|---|---|
| GET | `/api/competitions/{cid}/leaderboard/games/{gid}/teams` | Per-game team leaderboard |
| GET | `/api/competitions/{cid}/leaderboard/games/{gid}/players` | Per-game player leaderboard |
| GET | `/api/competitions/{cid}/leaderboard/teams` | Total team leaderboard |
| GET | `/api/competitions/{cid}/leaderboard/players` | Total player leaderboard |

## Frontend Layers

The frontend keeps route screens, feature components, API calls, and shared utilities separate.

- `pages` contains route-level screens.
- `features/<feature>/api` contains feature API calls.
- `features/<feature>/components` contains focused UI components.
- `shared/api` contains the generic API client.
- `shared/types` contains shared TypeScript types.

UI components do not call `fetch` directly.

### Competition Run Feature (`features/competition-run`)

| File | Role |
|---|---|
| `api/competitionRunApi.ts` | Start, getMatches, enterResults; thin wrappers for game/team/player reads |
| `api/leaderboardApi.ts` | Four leaderboard API calls |
| `components/GameStepNav` | Step-through control to navigate between games |
| `components/MatchCard` | Shows one match with status and edit button |
| `components/MatchResultForm` | Form to enter per-player scores, loads team/player names from API |
| `components/GameTeamLeaderboard` | Per-game team ranking table with dynamic column header |
| `components/GamePlayerLeaderboard` | Per-game player ranking table |
| `components/TotalTeamLeaderboard` | Overall team placement-point table |
| `components/TotalPlayerLeaderboard` | Overall player placement-point table |
| `pages/CompetitionRunPage` | Combines start button, game nav, matches tab, and leaderboard tabs |
