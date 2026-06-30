# Architecture

This document describes the architecture, layer boundaries, domain concepts, feature flows, and API surface for Gaming Night. For an overview of all documentation, see [README.md](README.md).

## Table of Contents

- [Backend Layers](#backend-layers)
- [Dependency Rules](#dependency-rules)
- [Domain Models](#domain-models)
- [Feature Flows](#feature-flows)
- [Authentication And Authorization](#authentication-and-authorization)
- [REST API](#rest-api)
- [Frontend Layers](#frontend-layers)

## Backend Layers

```text
infrastructure -> application -> domain
```

The domain is the center of the application. Domain models have no Spring, JPA, database, or HTTP annotations - they are pure Java business objects.

The application layer contains use cases and DTOs. Use case services own the user-facing workflows and coordinate validation, persistence, and mapping between layers.

The infrastructure layer contains adapters that implement the ports defined in the domain:

- **Controllers**: Map HTTP requests to use cases
- **Repository Adapters**: `Jpa*RepositoryAdapter` classes implement domain repository ports
- **Entity Classes**: Persistence models that are separate from domain models
- **Exception Handlers**: `GlobalExceptionHandler` converts application exceptions into HTTP responses

## Dependency Rules

- **Domain**: Must not import Spring, JPA, web, or database packages
- **Application**: May depend on domain and framework-free shared code
- **Infrastructure**: May depend on application, domain, and Spring
- **Controllers**: Must not contain business logic
- **Persistence**: Entities must not be returned from use cases or controllers

## Domain Models

| Model | Description |
|---|---|
| `Player` | A registered participant |
| `User` | A system user with a username, role, and required player link |
| `UserRole` | Role enum with `ADMIN` and `USER` |
| `Game` | A game with `GameType` (SCORE_BASED / TIME_BASED) and `CalculationMethod` (SUM / AVERAGE) |
| `Team` | A named group of players. Team names are unique across the application |
| `TeamName` | A seeded catalog entry used as the source for random generated team names |
| `Competition` | An event with ordered games and teams; can be started once |
| `Match` | A single matchup between two teams within a game of a competition |
| `PlayerResult` | A player's individual score or time in a match (value type, no identity) |

## Feature Flows

### Player Management

Administrators manage participants through `/api/players` and the `/players` frontend section. Players can be created, listed, edited, and deleted before they are assigned to teams.

### User Administration

Administrators manage system users through `/api/users` and the `/users` frontend section. A user has a unique username, password, one role (`ADMIN` or `USER`), and must be tied to an existing player. A player can only be tied to one user.

### Game Management

Administrators manage games through `/api/games` and the `/games` frontend section. Each game defines whether higher values or lower values rank better through `GameType`, and whether results are aggregated with `CalculationMethod.SUM` or `CalculationMethod.AVERAGE`.

### Team Management

Administrators manage teams through `/api/teams` and the `/teams` frontend section. Team names are validated in the domain and checked for case-insensitive uniqueness in the application layer before persistence. A database unique index also enforces name uniqueness.

### Competition Setup

Administrators manage competition setup through `/api/competitions` and the `/competitions` frontend section. A competition stores its name, date, single-match setting, ordered game IDs, team IDs, and started state. Setup changes validate referenced games, teams, and generated-team players before saving.

Auto-generated teams are created from selected players through `POST /api/competitions/{id}/generate-teams`. The use case shuffles players, fills teams according to the requested team size, distributes leftovers one per team, picks unused names from the seeded `team_names` catalog, saves the teams, and replaces the competition's assigned teams. Team names are unique across the app, including manually created teams, so generated names never reuse an existing team name. Started competitions cannot be edited or regenerated.

### Competition Run

1. Admin creates a competition via `POST /api/competitions` with game and team IDs.
2. Admin calls `POST /api/competitions/{id}/start`.
3. `CompetitionRunUseCaseService.start()` generates round-robin matches for every (game, team-pair) combination.
4. The competition is marked `started = true`; this cannot be reversed.
5. Admin enters results via `PUT /api/competitions/{cid}/matches/{mid}/results`.
6. Results are stored as `PlayerResult` entries attached to the `Match`.

### Leaderboards

`LeaderboardUseCaseService` computes four views:

- **Per-game team** - aggregate player results per team using the game's `CalculationMethod`; rank using `GameType`.
- **Per-game player** - same logic per individual player.
- **Total team** - apply placement points (1st=100, 2nd=90, ..., 10th=10) per game, sum across all games.
- **Total player** - same placement-point system per player.

The column header (`Total Score`, `Average Score`, `Total Time`, `Average Time`) is computed from the game's type and calculation method and returned alongside the leaderboard rows.

## Authentication And Authorization

Users log in through `POST /api/auth/login`. The backend verifies the password hash, returns a signed bearer token, and the frontend stores that token for subsequent API calls. If an API request returns `401 Unauthorized`, the frontend clears the stored token and user profile; the route guard then redirects the user to `/login`.

Role behavior:

- `ADMIN` users can access every backend endpoint and every frontend section.
- `USER` users can access `GET /api/users/me`, competition read endpoints, leaderboards, match reads, and supporting detail reads for games, teams, and players.
- Competition reads for `USER` accounts are filtered to competitions where the user's linked player belongs to one of the competition teams.
- Mutating setup and result-entry actions are admin-only.

The frontend mirrors these rules in navigation: admins see all tabs, while regular users see only `Competitions` and `My user`.

## REST API

### Authentication

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Log in and receive a bearer token |

### Players

| Method | Path | Description |
|---|---|---|
| POST | `/api/players` | Create player |
| GET | `/api/players` | List players |
| GET | `/api/players/{id}` | Get player |
| PUT | `/api/players/{id}` | Update player |
| DELETE | `/api/players/{id}` | Delete player |

### Users

| Method | Path | Description |
|---|---|---|
| POST | `/api/users` | Create user tied to a player |
| GET | `/api/users` | List users |
| GET | `/api/users/me` | Get the authenticated user's profile |
| GET | `/api/users/{id}` | Get user |
| PUT | `/api/users/{id}` | Update username, password, role, or player link |
| DELETE | `/api/users/{id}` | Delete user |

### Games

| Method | Path | Description |
|---|---|---|
| POST | `/api/games` | Create game |
| GET | `/api/games` | List games |
| GET | `/api/games/{id}` | Get game |
| PUT | `/api/games/{id}` | Update game |
| DELETE | `/api/games/{id}` | Delete game |

### Teams

| Method | Path | Description |
|---|---|---|
| POST | `/api/teams` | Create team |
| GET | `/api/teams` | List teams |
| GET | `/api/teams/{id}` | Get team |
| PUT | `/api/teams/{id}` | Update team |
| DELETE | `/api/teams/{id}` | Delete team |

### Competitions

| Method | Path | Description |
|---|---|---|
| POST | `/api/competitions` | Create competition with ordered games and teams |
| GET | `/api/competitions` | List competitions; regular users only receive competitions for their linked player |
| GET | `/api/competitions/{id}` | Get competition; regular users must be part of the competition as a player |
| PUT | `/api/competitions/{id}` | Update setup before start |
| DELETE | `/api/competitions/{id}` | Delete competition |
| POST | `/api/competitions/{id}/generate-teams` | Auto-generate teams from selected players |

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

### Players Feature (`features/players`)

| File | Role |
|---|---|
| `api/playersApi.ts` | listPlayers, getPlayer, createPlayer, updatePlayer, deletePlayer |
| `components/PlayerList` | Table of players with edit and delete actions |
| `components/PlayerForm` | Form for player names |
| `pages/PlayersPage` | List page with delete confirmation |
| `pages/CreatePlayerPage` | Create form wired to POST /api/players |
| `pages/EditPlayerPage` | Edit form wired to PUT /api/players/{id} |

### Users Feature (`features/users`)

| File | Role |
|---|---|
| `api/usersApi.ts` | listUsers, getUser, getCurrentUser, createUser, updateUser, deleteUser |
| `components/UserList` | Table of users with role, player link, edit, and delete actions |
| `components/UserForm` | Form for username, password, role, and player assignment |
| `pages/UsersPage` | List page with delete confirmation |
| `pages/CreateUserPage` | Create form wired to POST /api/users |
| `pages/EditUserPage` | Edit form wired to PUT /api/users/{id} |

### Games Feature (`features/games`)

| File | Role |
|---|---|
| `api/gamesApi.ts` | listGames, getGame, createGame, updateGame, deleteGame |
| `components/GameList` | Table of games with type, calculation method, edit and delete actions |
| `components/GameForm` | Form for name, game type (radio), calculation method (radio), and description |
| `pages/GamesPage` | List page with delete confirmation |
| `pages/CreateGamePage` | Create form wired to POST /api/games |
| `pages/EditGamePage` | Edit form wired to PUT /api/games/{id} |

### Teams Feature (`features/teams`)

| File | Role |
|---|---|
| `api/teamsApi.ts` | listTeams, getTeam, createTeam, updateTeam, deleteTeam |
| `components/TeamList` | Table of teams with edit and delete actions |
| `components/TeamForm` | Form for team name and assigned player IDs |
| `pages/TeamsPage` | List page with delete confirmation |
| `pages/CreateTeamPage` | Create form wired to POST /api/teams |
| `pages/EditTeamPage` | Edit form wired to PUT /api/teams/{id} |

### Competitions Feature (`features/competitions`)

| File | Role |
|---|---|
| `api/competitionsApi.ts` | Competition CRUD and generate-teams API calls |
| `components/CompetitionList` | Table of competitions with setup and started status |
| `components/CompetitionForm` | Form for competition settings, ordered games, and teams |
| `components/GenerateTeamsWizard` | Player selection and team-size workflow for generating teams |
| `pages/CompetitionsPage` | List page with delete confirmation |
| `pages/CreateCompetitionPage` | Create form wired to POST /api/competitions |
| `pages/EditCompetitionPage` | Edit form wired to PUT /api/competitions/{id} |

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
