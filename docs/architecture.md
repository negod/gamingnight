# Architecture

This document describes the architecture, layer boundaries, domain concepts, feature flows, and API surface for Gaming Night. For an overview of all documentation, see [README.md](README.md).

## Table of Contents

- [Backend Layers](#backend-layers)
- [Dependency Rules](#dependency-rules)
- [Domain Models](#domain-models)
- [Feature Flows](#feature-flows)
- [Security Architecture](#security-architecture)
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
| `Competition` | An event with ordered games, teams, an admin-controlled registration-open flag, and pre-start player registrations; can be started once |
| `Match` | A single matchup between two teams within a game of a competition |
| `PlayerResult` | A player's individual score or time in a match (value type, no identity) |

## Feature Flows

### Player Management

Administrators manage participants through `/api/players` and the `/players` frontend section. Players can be created, listed, edited, and deleted before they are assigned to teams.

### User Administration

Administrators manage system users through `/api/users` and the `/users` frontend section. A user has a unique username, password, one role (`ADMIN` or `USER`), and must be tied to an existing player. A player can only be tied to one user.

### Game Management

Administrators manage games through `/api/games` and the `/games` frontend section. Each game defines match setup, participant rules, result type, winner rule, scoring rule, tie-breakers, optional validation, optional time limits, and optional bonus rules. The frontend game form includes optional presets such as Racing, Sports, Action, Fighting, Strategy, Party, and Co-op to fill those rule fields as a starting point; admins can still adjust every rule before saving.

### Team Management

Administrators manage teams through `/api/teams` and the `/teams` frontend section. Team names are validated in the domain and checked for case-insensitive uniqueness in the application layer before persistence. A database unique index also enforces name uniqueness.

### Competition Setup

Administrators manage competition setup through `/api/competitions` and the `/competitions` frontend section. A competition stores its name, date, single-match setting, registration-open flag, ordered game IDs, team IDs, registered player IDs, and started state. Setup changes validate referenced games, teams, and generated-team players before saving. Authenticated users can register or unregister their linked player only while the competition is not started and `registrationOpen` is enabled.

Auto-generated teams are created from selected players through `POST /api/competitions/{id}/generate-teams`. The use case shuffles players, fills teams according to the requested team size, distributes leftovers one per team, picks unused names from the seeded `team_names` catalog, saves the teams, and replaces the competition's assigned teams. Registered players are preselected in the frontend generate-teams workflow. Team names are unique across the app, including manually created teams, so generated names never reuse an existing team name. Started competitions cannot be edited, regenerated, registered for, or unregistered from.

### Competition Run

1. Admin creates a competition via `POST /api/competitions` with game and team IDs.
2. Admin calls `POST /api/competitions/{id}/start`.
3. `CompetitionRunUseCaseService.start()` generates round-robin matches for every (game, team-pair) combination.
4. The competition is marked `started = true`; this cannot be reversed.
5. Admin enters results via `PUT /api/competitions/{cid}/matches/{mid}/results`.
6. Results are stored as `PlayerResult` entries attached to the `Match`.

Player result values accept scores or times from `-99999.0` through `99999.0` with at most two decimal places. The HTTP request DTO enforces that range and precision before use-case handling, and the domain `PlayerResult` rejects non-finite values (`NaN` and infinity) for all entry paths so leaderboard aggregation cannot be corrupted by invalid floating-point values.

### Leaderboards

`LeaderboardUseCaseService` computes four views:

- **Per-game team** - aggregate player results per team using the game's `CalculationMethod`; rank using `GameType`.
- **Per-game player** - same logic per individual player.
- **Total team** - apply placement points (1st=100, 2nd=90, ..., 10th=10) per game, sum across all games.
- **Total player** - same placement-point system per player.

The column header (`Total Score`, `Average Score`, `Total Time`, `Average Time`) is computed from the game's type and calculation method and returned alongside the leaderboard rows.
Aggregated decimal result values are rounded to at most three decimal places in per-game leaderboards and match result displays.

## Security Architecture

### Authentication

Authentication is stateless. `POST /api/auth/signup` (public, always creates a `USER`-role account plus a matching `Player`) and `POST /api/auth/login` (public) both return a signed bearer token from `TokenService`. The token is a custom HMAC-SHA256-signed value (`base64url(payload).base64url(signature)` — not an RFC 7519 JWT) carrying the user id, username, role, player id, and a 12-hour expiry. `JwtAuthenticationFilter` verifies the signature in constant time and rejects expired or malformed tokens before `SecurityConfig` authorizes the request. `SessionCreationPolicy` is `STATELESS` — no server-side session is ever created. The frontend stores the token, attaches it as `Authorization: Bearer <token>` on every request, and on any `401 Unauthorized` response clears the stored token/profile and redirects to `/login`.

The signing secret comes from `app.auth.token-secret` (env `APP_AUTH_TOKEN_SECRET`). Local development supplies `dev-only-change-me` through `application-local.yml`; every real deployment must set `APP_AUTH_TOKEN_SECRET` explicitly. Without it, the application fails fast on startup instead of silently using a known signing secret.

Passwords are hashed with PBKDF2WithHmacSHA256 (120,000 iterations, 256-bit derived key, random 16-byte salt per user) and stored as `pbkdf2$<iterations>$<saltB64>$<hashB64>` (`PasswordService`); verification uses a constant-time comparison.

### Authorization

Two roles exist: `ADMIN` and `USER` (`UserRole`). Authorization is enforced at two independent layers that must both agree before a request succeeds:

1. **URL-level** (`SecurityConfig.filterChain`) — an allow-list of public and `USER`-readable routes; every other route defaults to `ADMIN`-only.
2. **Method-level** (`@PreAuthorize("hasRole('ADMIN')")`) on every mutating use-case method, so a missing or misconfigured URL rule cannot by itself expose a write path.

| Capability | ADMIN | USER |
|---|---|---|
| `POST /api/auth/login`, `POST /api/auth/signup` | Public | Public |
| `GET /api/users/me` | ✅ | ✅ |
| `GET` on competitions, matches, and leaderboards (`/api/competitions/**`) | ✅ (all) | ✅, open setup competitions are visible; started competitions are filtered to competitions where the user's linked player is registered or belongs to a competition team |
| `GET` a single game, team, or player (`/api/games/*`, `/api/teams/*`, `/api/players/*`) | ✅ | ✅ |
| Register/unregister own player for an open setup competition | ✅ | ✅ |
| List all players, games, teams, or users; any create/update/delete on players, games, teams, competitions, or users; start a competition; enter/edit results; generate teams | ✅ | ❌ (falls through to the `ADMIN`-only default) |

The frontend mirrors these rules in navigation: admins see every tab, while regular users see only `Competitions` and `My user`.

### Transport And Request Hardening

- **CORS** (`WebConfig`): `/api/**` is restricted to the origins listed in `app.cors.allowed-origins` (env `CORS_ALLOWED_ORIGINS`, default `http://localhost:5173`), the methods the API actually uses, and exposes only the `Authorization` response header.
- **Security headers** (`SecurityConfig`): Content-Security-Policy (`default-src 'self'`), `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, a restrictive `Permissions-Policy` (camera/microphone/geolocation disabled), plus Spring Security's default `X-Content-Type-Options: nosniff`.
- **CSRF** is disabled. This is safe here because the API is fully stateless (no cookies, no server-side session) and only honors a bearer token the frontend attaches explicitly — there is no ambient browser-held credential for a forged cross-site request to ride on.
- **Rate limiting** (`RateLimitingFilter`, Bucket4j, keyed per client IP): `POST /api/auth/login` — 10/min; `POST /api/auth/signup` — 5/min; `POST /api/users` — 5/min; `POST /api/competitions/{id}/start` — 10/min; `PUT /api/competitions/{cid}/matches/{mid}/results` — 30/min. Requests over the limit get `429 Too Many Requests` with a `Retry-After` header and the standard API error envelope.
- **Actuator**: only `/actuator/health` is exposed, with `show-details: never`; `info`, `env`, and all other actuator endpoints are unpublished.

### Threat Model

| Threat | Mitigation |
|---|---|
| Broken access control | Two independent enforcement layers (URL allow-list + `@PreAuthorize` on every mutating use case), so one missing check can't expose a write path; competition reads are further scoped to the requesting user's own player/teams. |
| CSRF | Not applicable — stateless bearer-token auth with no cookies or sessions, plus CORS restricts which origins can reach the API at all. |
| Injection (SQL/JPQL) | All persistence goes through Spring Data derived-query repositories (`Jpa*RepositoryAdapter`); the codebase has no native or string-concatenated `@Query` usage. |
| Insecure defaults | Database credentials (SEC-5), actuator exposure (SEC-4), and the JWT signing secret have no silent production fallback. Local-only defaults live in `application-local.yml`; non-local startup requires explicit secrets. |
| Excessive data exposure | `GlobalExceptionHandler` returns only whitelisted messages/field errors for known exception types; unhandled exceptions fall back to Spring Boot's default error body, which omits stack traces by default (`server.error.include-stacktrace` is not overridden from `never`). |

### OWASP ASVS Compliance

**Target: ASVS Level 1**, with several Level 2 controls already in place.

Met: strong salted password hashing (V2.4), stateless token-based session management (V3), RBAC enforced at two independent layers (V4), request validation on every mutating DTO plus domain-level invariants (V5), generic/whitelisted error responses (V7), CORS/CSP/security-header hardening (V14), and automated dependency CVE scanning via the OWASP dependency-check Maven plugin (SEC-6).

Known gaps, not yet resolved: no token revocation/blacklist (a leaked token stays valid until its 12-hour expiry), no account lockout beyond per-IP rate limiting, no MFA, and no dedicated security-event audit log. These should be closed before claiming full Level 2.

## REST API

### Authentication

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Log in and receive a bearer token |
| POST | `/api/auth/signup` | Self-register a `USER` account with a linked player and receive a bearer token |

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
| POST | `/api/competitions/{id}/registrations/me` | Register the authenticated user's linked player when registration is open before start |
| DELETE | `/api/competitions/{id}/registrations/me` | Unregister the authenticated user's linked player when the competition has not started |

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
| `components/GameList` | Table of games with rule summary, edit and delete actions |
| `components/GameForm` | Form for basic details, optional game type presets, match setup, result rules, scoring, rotation, validation, time limits, and bonus rules |
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
| `api/competitionsApi.ts` | Competition CRUD, registration, and generate-teams API calls |
| `components/CompetitionList` | Table of competitions with setup, started status, and user registration actions |
| `components/CompetitionForm` | Form for competition settings, ordered games, and teams |
| `components/GenerateTeamsWizard` | Player selection and team-size workflow for generating teams, preselecting registered players |
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
