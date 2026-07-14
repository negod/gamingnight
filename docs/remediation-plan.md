# Remediation Plan — Gaming Night

Generated from a full audit of the project against the principles in `docs/ai-instructions.md`.

---

## How to read this plan

Each item carries an **ID** (e.g. `SEC-1`), a **severity** (`Critical / High / Medium / Low`), and a **model assignment** with rationale. Items within each model section are ordered: critical first, then descending severity. Complete critical items before medium ones.

**Model selection rationale**

| Model | Assigned work |
|---|---|
| **Claude** | Security architecture, complex multi-file refactoring, DDD invariant design, architectural documentation — tasks requiring judgment about design trade-offs and cross-cutting consequences. |
| **Codex** | Mechanical test writing from existing patterns, adding validation annotations, boilerplate plugin/config additions, and rate-limiting middleware wiring — tasks that are largely pattern-completion against already-established code. |
| **Mistral** | Configuration-only fixes, documentation updates, YAML/XML simple edits, and minor code cleanups where no design decision is needed. |

---

## Priority 0 — Critical security issues

These must be addressed before any new feature work.

---

### Claude

#### SEC-1 · Implement Spring Security with role-based access control `[Resolved]`

**Status**: Resolved. Spring Security 6 added with JWT-based stateless authentication.

- `spring-boot-starter-security` + `spring-security-test` added to `pom.xml`.
- `JwtAuthenticationFilter extends OncePerRequestFilter` validates Bearer tokens via `TokenService`, sets `SecurityContextHolder` + `AuthContext` (ThreadLocal preserved for backward compat).
- `SecurityConfig` (`@EnableWebSecurity @EnableMethodSecurity`): CSRF disabled, stateless sessions, role rules (USER allowed on GET competitions/users-me/single-resource; everything else ADMIN).
- `AuthInterceptor` deleted; `WebConfig` cleaned up.
- `@PreAuthorize("hasRole('ADMIN')")` on `CompetitionUseCaseService` (create/update/delete/generateTeams), `CompetitionRunUseCaseService` (start/enterResults), `UserUseCaseService` (create/list/getById/update/delete).
- HTTP security headers in `SecurityConfig` (also covers SEC-2): CSP, X-Frame-Options, Referrer-Policy, Permissions-Policy.
- All `@WebMvcTest` tests updated: `@Import(SecurityConfig.class)` + `@WithMockUser(roles = "ADMIN")`.
- `AuthorizationTest` added: unauthenticated → 401, USER on admin endpoint → 403, USER on competition list → 200, ADMIN create → 201.
- All 145 tests pass.

**Files affected**
- `backend/pom.xml`
- `backend/src/main/java/se/backede/infrastructure/config/SecurityConfig.java` (new)
- `backend/src/main/java/se/backede/infrastructure/security/JwtAuthenticationFilter.java` (new)
- `backend/src/main/java/se/backede/infrastructure/security/AuthInterceptor.java` (deleted)
- `backend/src/main/java/se/backede/infrastructure/config/WebConfig.java`
- `backend/src/main/java/se/backede/application/usecase/CompetitionUseCaseService.java`
- `backend/src/main/java/se/backede/application/usecase/CompetitionRunUseCaseService.java`
- `backend/src/main/java/se/backede/application/usecase/UserUseCaseService.java`
- All six `@WebMvcTest` test classes + `AuthorizationTest.java` (new)

---

#### SEC-2 · Configure HTTP security headers `[Resolved]`

**Status**: Resolved as part of SEC-1. `SecurityConfig` configures all required headers via Spring Security's `headers()` DSL: Content-Security-Policy, X-Frame-Options (DENY), Referrer-Policy (strict-origin-when-cross-origin), and Permissions-Policy (camera/microphone/geolocation disabled). Spring Security also adds X-Content-Type-Options by default.

**Files affected**
- `backend/src/main/java/se/backede/infrastructure/config/SecurityConfig.java`

---

### Codex

#### SEC-3 · Add rate limiting to sensitive endpoints `[Resolved]`

**Status**: Resolved. Bucket4j-backed per-IP rate limiting is applied before controller handling for the sensitive mutating endpoints:

- `POST /api/users` — 5 requests/minute per IP.
- `POST /api/competitions/*/start` — 10 requests/minute per IP.
- `PUT /api/competitions/*/matches/*/results` — 30 requests/minute per IP.

Requests over the limit return `429 Too Many Requests`, a `Retry-After` header, and the standard API error envelope.

`RateLimitingFilterTest` verifies each threshold returns `429` after the configured allowance is exceeded. Full backend test suite: 178 tests pass.

**Files affected**
- `backend/pom.xml`
- `backend/src/main/java/se/backede/infrastructure/config/RateLimitingFilter.java` (new)
- `backend/src/main/resources/application.yml` (Bucket4j config)
- `backend/src/test/java/se/backede/infrastructure/web/RateLimitingFilterTest.java` (new)
- `docs/architecture.md`

---

## Priority 1 — Architecture and DDD

---

### Claude

#### CA-1 / DDD-1 · Move `Competition.start()` pre-conditions into the domain model `[Resolved]`

**Status**: Resolved. `Competition.start()` now guards its own invariants — it throws `DomainValidationException` when there are fewer than 2 team IDs or no game IDs. The duplicate pre-condition guards were removed from `CompetitionRunUseCaseService`, which now calls `competition.start(now)` up front (before generating any matches) so the domain validates before the use case does any work; only the `started` check (not part of this item) remains in the service. `CompetitionTest` covers 0/1 team, 0 games, and the 2-teams-1-game success path; `CompetitionRunUseCaseServiceTest` verifies the use case propagates (not duplicates) the domain exception for both too-few-teams and no-games. All 178 backend tests pass.

**Problem**: `CompetitionRunUseCaseService.java:51-58` checks `teamIds.size() >= 2` and `!gameIds.isEmpty()` before calling `competition.start()`. The domain method itself does not guard these invariants — it blindly flips the flag. This violates DDD (domain objects must protect their own invariants) and Clean Architecture (business rules belong in the domain, not the use case).

**What to do**

1. In `backend/src/main/java/se/backede/domain/model/Competition.java`, modify `start()` to throw `DomainValidationException` if fewer than 2 team IDs or no game IDs.
2. Remove the duplicate pre-condition guards from `CompetitionRunUseCaseService.java`.
3. Update `CompetitionTest.java` to cover:
   - Calling `start()` with 0 teams → `DomainValidationException`.
   - Calling `start()` with 1 team → `DomainValidationException`.
   - Calling `start()` with 0 games → `DomainValidationException`.
   - Calling `start()` with 2 teams and 1 game → success.
4. Adjust the use-case test to verify the use case propagates (not duplicates) the domain exception.

**Files affected**
- `backend/src/main/java/se/backede/domain/model/Competition.java`
- `backend/src/main/java/se/backede/application/usecase/CompetitionRunUseCaseService.java`
- `backend/src/test/java/se/backede/domain/model/CompetitionTest.java`
- `backend/src/test/java/se/backede/application/usecase/CompetitionRunUseCaseServiceTest.java`

---

#### SO-1 / CC-1 · Split `LeaderboardUseCaseService` and remove duplicate aggregation logic `[Resolved]`

**Status**: Resolved (DRY portion). `computeTeamRows` and `computePlayerRows` now delegate to a single generic `computeRows(competitionId, gameId, game, keyExtractor, nameResolver, rowFactory)` method, parameterised by a `Function<PlayerResult, UUID>` key extractor, a `Function<UUID, String>` name resolver, and a `LeaderboardRowFactory<R>` functional interface for constructing the row DTO. The two ~30-line near-duplicate methods are now 4-line delegations. The service was **not** split into `GameLeaderboardUseCaseService` / `TotalLeaderboardUseCaseService`: `getTotalTeamLeaderboard` and `getTotalPlayerLeaderboard` call `computeTeamRows`/`computePlayerRows` internally per game, so the two scopes share both port dependencies and logic — a split would force either duplicated aggregation or a cross-service dependency, with no SRP benefit. `LeaderboardController` is unchanged. All existing `LeaderboardUseCaseServiceTest` cases (ranking, tie-break, header selection, both team/player and game/total scopes) pass unchanged against the refactored implementation — 181 backend tests pass overall.

**Problem**: `LeaderboardUseCaseService.java` is 214 lines with four responsibilities (per-game team, per-game player, total team, total player) and two near-identical 30-line aggregation methods (`computeTeamRows`, `computePlayerRows`). Violates SRP (SOLID) and DRY (Clean Code).

**What to do**

1. Extract a generic private method (or a package-private utility class `LeaderboardAggregator`) parameterised by a key-extractor function:
   ```java
   private <K> Map<K, RowAccumulator> aggregate(
       List<PlayerResult> results, Function<PlayerResult, K> keyExtractor) { ... }
   ```
2. Replace both `computeTeamRows` and `computePlayerRows` with calls to this method.
3. Consider splitting into two services — `GameLeaderboardUseCaseService` and `TotalLeaderboardUseCaseService` — if the two scopes have different port dependencies, update `LeaderboardController` accordingly.
4. Update `LeaderboardUseCaseServiceTest` to cover the refactored structure.

**Files affected**
- `backend/src/main/java/se/backede/application/usecase/LeaderboardUseCaseService.java`
- `backend/src/test/java/se/backede/application/usecase/LeaderboardUseCaseServiceTest.java`
- `backend/src/main/java/se/backede/infrastructure/web/LeaderboardController.java` (if service is split)

---

#### SO-2 · Extract `GenerateTeamsUseCaseService` from `CompetitionUseCaseService` `[Resolved]`

**Status**: Resolved. `GenerateTeamsUseCaseService` (new) owns the shuffle, name-pool selection, and team-creation workflow previously in `CompetitionUseCaseService.generateTeams()`, and depends on `CompetitionRepositoryPort`, `TeamRepositoryPort`, `PlayerRepositoryPort`, `TeamNameRepositoryPort`. `CompetitionUseCaseService` dropped `TeamNameRepositoryPort` and `PlayerRepositoryPort` entirely; it keeps `TeamRepositoryPort` because `validateTeamsExist` (CRUD validation) and `playerHasTeamInCompetition` (player-scoped access checks used by `listForPlayer`/`getByIdForPlayer`/`playerCanAccessCompetition`) are unrelated to team generation and still need it — the plan's port list didn't account for these pre-existing uses. `CompetitionController` now injects both services and routes `POST /{id}/generate-teams` to `GenerateTeamsUseCaseService`. Generate-teams tests moved from `CompetitionUseCaseServiceTest` into a new `GenerateTeamsUseCaseServiceTest` (plus one added case for a missing competition); `CompetitionControllerTest` and `AuthorizationTest` (both `@WebMvcTest(CompetitionController.class)`) got a `GenerateTeamsUseCaseService` mock bean. All 190 backend tests pass.

**Problem**: `CompetitionUseCaseService` injects five repository ports and owns both competition CRUD and team-generation workflow. Violates SRP.

**What to do**

1. Create `backend/src/main/java/se/backede/application/usecase/GenerateTeamsUseCaseService.java` that owns the shuffle, name-pool selection, and team creation logic currently in `CompetitionUseCaseService.generateTeams()`.
2. `GenerateTeamsUseCaseService` needs: `TeamRepositoryPort`, `PlayerRepositoryPort`, `TeamNameRepositoryPort`.
3. `CompetitionUseCaseService` retains: `CompetitionRepositoryPort`, `GameRepositoryPort`.
4. Update `CompetitionController` to inject and call `GenerateTeamsUseCaseService` for the generate-teams endpoint.
5. Write `GenerateTeamsUseCaseServiceTest` following the same pattern as `CompetitionUseCaseServiceTest`.

**Files affected**
- `backend/src/main/java/se/backede/application/usecase/CompetitionUseCaseService.java`
- `backend/src/main/java/se/backede/application/usecase/GenerateTeamsUseCaseService.java` (new)
- `backend/src/main/java/se/backede/infrastructure/web/CompetitionController.java`
- `backend/src/test/java/se/backede/application/usecase/CompetitionUseCaseServiceTest.java`
- `backend/src/test/java/se/backede/application/usecase/GenerateTeamsUseCaseServiceTest.java` (new)

---

#### CA-2 / FE-1 · Lift data loading out of `MatchResultForm` `[Medium]`

**Problem**: `frontend/src/features/competition-run/components/MatchResultForm.tsx` calls `getTeam()` and `getPlayer()` directly in a `useEffect`. All other components receive data as props. This makes the component stateful and untestable without mocking the API layer.

**What to do**

1. Move the `getTeam` / `getPlayer` calls to `CompetitionRunPage.tsx` (or extract a custom hook `useMatchDetails(matchId)` in `features/competition-run/hooks/`).
2. Pass team and player data as props to `MatchResultForm`.
3. `MatchResultForm` becomes a pure controlled form component — no network calls, no `useEffect` for loading.
4. Update `MatchResultForm.test.tsx` (see TDD-6) to test the form in isolation with static prop data.

**Files affected**
- `frontend/src/features/competition-run/components/MatchResultForm.tsx`
- `frontend/src/pages/CompetitionRunPage.tsx`
- `frontend/src/features/competition-run/hooks/useMatchDetails.ts` (new, optional)

---

#### DOC-2 · Add security section to `docs/architecture.md` `[Medium]`

**Problem**: The AI instructions require documenting the threat model and security decisions for non-trivial features. Architecture.md has no security section.

**What to do**

After SEC-1 is implemented, add a `## Security Architecture` section to `docs/architecture.md` covering:
- Authentication strategy (session vs JWT — whichever was chosen).
- Authorisation model (role matrix: which roles can call which endpoints).
- Threat model: the top five threats considered (CSRF, broken access control, injection, insecure defaults, excessive data exposure) and how each is mitigated.
- OWASP ASVS level target (Level 1, 2, or 3) and current compliance status.

**Files affected**
- `docs/architecture.md`

---

## Priority 2 — DDD and validation

---

### Codex

#### DDD-2 / SEC-7 · Add range validation to `PlayerResult.value` `[Resolved]`

**Status**: Resolved. `PlayerResultInput.value` now requires a value in the range `-99999.0` through `99999.0` with at most five integer digits and two decimal places. The range is intentionally wide enough for score and time based games while bounding leaderboard impact from malicious or accidental extreme values.

The domain `PlayerResult` compact constructor rejects non-finite values (`NaN` and infinity) with `DomainValidationException`, protecting non-HTTP entry paths and persistence rehydration.

Coverage added:

- `CompetitionRunUseCaseServiceTest` verifies `NaN` and infinity inputs are rejected.
- `CompetitionRunControllerTest` verifies out-of-range result values return `400 Bad Request`.

Focused tests pass: 18 tests.

**Files affected**
- `backend/src/main/java/se/backede/application/dto/PlayerResultInput.java` (or equivalent DTO)
- `backend/src/main/java/se/backede/domain/model/PlayerResult.java`
- `backend/src/test/java/se/backede/application/usecase/CompetitionRunUseCaseServiceTest.java`
- `backend/src/test/java/se/backede/infrastructure/web/CompetitionRunControllerTest.java`

---

## Priority 3 — Missing tests

---

### Codex

#### TDD-1 · Write `TeamTest.java` `[Resolved]`

**Status**: Resolved by adding `backend/src/test/java/se/backede/domain/model/TeamTest.java`.

Coverage added:

- Valid construction with trimmed name and player IDs.
- `null` player list becomes empty.
- Blank name -> `DomainValidationException`.
- `null` name -> `DomainValidationException`.
- Name over 120 characters -> `DomainValidationException`.
- Update preserves identity and creation timestamp while trimming the new name.

Verification note: `mvn test -Dtest=TeamTest` could not complete because the current worktree has unrelated user-management compile errors around the new `User.email` field wiring.

---

#### TDD-2 · Write `TeamControllerTest.java` `[Resolved]`

**Status**: Resolved by adding `backend/src/test/java/se/backede/infrastructure/web/TeamControllerTest.java`.

Coverage added:

- `GET /api/teams` returns list.
- `GET /api/teams/{id}` returns team.
- `GET /api/teams/{id}` with unknown ID returns 404.
- `POST /api/teams` with valid body returns 201.
- `POST /api/teams` with blank name returns 400.
- `PUT /api/teams/{id}` updates and returns updated team.
- `DELETE /api/teams/{id}` returns 204.
- `DELETE /api/teams/{id}` with unknown ID returns 404.

Verification:

```bash
mvn -f backend/pom.xml -Dtest=TeamControllerTest test
```

---

#### TDD-3 · Write Testcontainers persistence tests `[Medium]`

**Problem**: No persistence tests exist. JPA adapter correctness, FK constraints, and Liquibase schema validity are untested.

**What to do**

Create at minimum:
- `backend/src/test/java/se/backede/infrastructure/persistence/JpaPlayerRepositoryAdapterTest.java`
- `backend/src/test/java/se/backede/infrastructure/persistence/JpaCompetitionRepositoryAdapterTest.java`

Each test class must:
1. Use `@SpringBootTest` + `@Testcontainers` with a `@Container PostgreSQLContainer`.
2. Verify save, find-by-id, find-all, and delete round-trip through the real JPA adapter.
3. Verify Liquibase applies all changelogs without error (this is implicit — if the container starts, schema applied).
4. Verify FK constraints (e.g., deleting a player that is part of a competition result should either cascade or throw a meaningful exception).

**Pattern to follow**: see `testing.md` section on Testcontainers for the annotation setup. Look at `CompetitionRunUseCaseServiceTest.java` for how ports are wired in tests, then adapt for the real adapter.

---

#### TDD-4 · Write `GameForm.test.tsx` and `GameList.test.tsx` `[Resolved]`

**Status**: Resolved by adding:

- `frontend/src/features/games/components/GameForm.test.tsx`
- `frontend/src/features/games/components/GameList.test.tsx`

Coverage added:

- `GameForm` submits game name, selected game type, selected calculation method, and description.
- `GameForm` shows a validation error for missing name.
- `GameList` renders the empty state.
- `GameList` renders game labels and edit links.
- `GameList` calls the delete handler with the selected game id.

Verification:

```bash
cd frontend && npx vitest run src/features/games/components/GameForm.test.tsx src/features/games/components/GameList.test.tsx
```

---

#### TDD-5 · Write `TeamForm.test.tsx` and `TeamList.test.tsx` `[Resolved]`

**Status**: Resolved by adding:

- `frontend/src/features/teams/components/TeamForm.test.tsx`
- `frontend/src/features/teams/components/TeamList.test.tsx`

Coverage added:

- `TeamForm` submits team name and selected player IDs.
- `TeamForm` shows a validation error for missing name.
- `TeamForm` renders the empty player state.
- `TeamList` renders the empty state.
- `TeamList` renders team name, player count, and edit link.
- `TeamList` calls the delete handler with the selected team id.

Verification:

```bash
cd frontend && npx vitest run src/features/teams/components/TeamForm.test.tsx src/features/teams/components/TeamList.test.tsx
```

---

#### TDD-6 · Write `MatchCard.test.tsx` and `MatchResultForm.test.tsx` `[Medium]`

**Problem**: `MatchResultForm` is the most complex frontend component (async loading, controlled form, submission). No tests.

**Do this after CA-2/FE-1** (MatchResultForm refactor), so the component is stateless and can be tested with static props.

Then create:
- `MatchCard.test.tsx`: renders match status; renders correct team/player names.
- `MatchResultForm.test.tsx`: renders all result inputs; submitting calls `onSubmit` with correct payload; cancelling calls `onCancel`.

---

## Priority 4 — Configuration and documentation fixes

---

### Mistral

#### SEC-4 · Remove `info` from actuator exposure `[Medium]`

**Problem**: `application.yml` exposes `health,info`. The `info` endpoint can leak build metadata. Only `health` should be public.

**What to do**

In `backend/src/main/resources/application.yml`, change:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
```

Remove `info` from the `include` list. If build info is needed internally, secure the `info` endpoint behind `ADMIN` role once Spring Security (SEC-1) is in place.

---

#### SEC-5 · Remove hardcoded default database credentials `[Medium]`

**Problem**: `application.yml` falls back to `gaming-night` / `gaming-night` if env vars are not set. This silently allows connection with a well-known password.

**What to do**

In `backend/src/main/resources/application.yml`, replace:

```yaml
username: ${SPRING_DATASOURCE_USERNAME:gaming-night}
password: ${SPRING_DATASOURCE_PASSWORD:gaming-night}
```

with:

```yaml
username: ${SPRING_DATASOURCE_USERNAME}
password: ${SPRING_DATASOURCE_PASSWORD}
```

Spring Boot will throw `IllegalArgumentException` on startup if the variables are not set, which is the correct fail-fast behaviour. Update `docker-compose.yml` and `docs/deployment.md` to confirm both variables are always supplied.

---

#### SEC-6 · Add OWASP dependency-check to Maven build `[Medium]`

**Problem**: No automated CVE scanning. The AI instructions require flagging known CVEs before shipping.

**What to do**

Add to the `<build><plugins>` section of `backend/pom.xml`:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.3</version>
    <executions>
        <execution>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFiles>
            <suppressionFile>owasp-suppressions.xml</suppressionFile>
        </suppressionFiles>
    </configuration>
</plugin>
```

Create an empty `backend/owasp-suppressions.xml` for future false-positive suppressions. Run `mvn dependency-check:check` and resolve or suppress any findings before merging.

---

#### DOC-1 · Update `deployment.md` changelog layout `[Low]`

**Problem**: `docs/deployment.md` does not list `0010-create-users.yaml` in its changelog section.

**What to do**

Update the "Current layout" or equivalent block in `docs/deployment.md` to include `0010-create-users.yaml`. Also verify the seeded users described in the README exist in `0008-seed-test-data.yaml` (see LIQ-2).

---

#### LIQ-1 · Document current irregular Liquibase prefix state `[Low]`

**Problem**: The `changes/` directory has duplicate prefixes (`0002`, `0003`) and gaps (`0001`, `0004`, `0005`) because earlier changesets were deleted. Existing changesets cannot be renamed (they are applied). Future ones must follow strict sequential ordering.

**What to do**

Add a note at the top of the Liquibase section in `docs/ai-instructions.md`:

> **Note on existing changesets**: Due to historical deletions, the numbering has gaps and duplicates at 0002 and 0003. These cannot be renamed as they are already applied. All new changesets must use strictly sequential numbers starting from the highest existing prefix + 1 (currently `0012`).

Also add a comment to `db.changelog-master.yaml` at the top listing the current sequence for reference.

---

#### LIQ-2 · Seed users in current migrations `[Resolved]`

**Status**: User seed data now lives in append-only migrations:

- `0010-create-users.yaml` creates the `users` table and seeds `admin` and `user`.
- `0011-add-user-passwords.yaml` adds password hashes for those seeded users.

The README and deployment guide document the development credentials as `admin` / `admin` and `user` / `user`. Change these passwords before exposing a non-local environment.

---

#### CC-2 · Remove empty `domain/service` package `[Low]`

**Problem**: `backend/src/main/java/se/backede/domain/service/` contains only a `package-info.java` with no classes. Dead structural noise.

**What to do**

Delete the directory. If domain services are needed in future, create the directory then. No other files reference this package.

---

#### CC-3 · Standardise `@Transactional` across JPA adapters `[Low]`

**Problem**: `JpaMatchRepositoryAdapter` uses a class-level `@Transactional` then overrides individual reads with `@Transactional(readOnly = true)`. Other adapters use method-level annotations only. The inconsistency creates confusion about the default transaction mode.

**What to do**

Choose one pattern and apply it to all five `Jpa*RepositoryAdapter` classes:

- **Preferred**: no class-level `@Transactional`; annotate write methods with `@Transactional` and read methods with `@Transactional(readOnly = true)` individually. This is the most explicit and consistent with Spring best practices.

Affected files: all `Jpa*RepositoryAdapter.java` files in `backend/src/main/java/se/backede/infrastructure/persistence/`.

---

## Summary checklist

| ID | Severity | Model | Status |
|---|---|---|---|
| SEC-1 | Critical | Claude | ✅ |
| SEC-2 | Critical | Claude | ✅ |
| SEC-3 | Critical | Codex | ✅ |
| CA-1 / DDD-1 | High | Claude | ✅ |
| SO-1 / CC-1 | High | Claude | ✅ |
| SO-2 | Medium | Claude | ✅ |
| CA-2 / FE-1 | Medium | Claude | ☐ |
| DOC-2 | Medium | Claude | ☐ |
| DDD-2 / SEC-7 | Medium | Codex | ✅ |
| TDD-1 | Medium | Codex | ☑ |
| TDD-2 | Medium | Codex | ✅ |
| TDD-3 | Medium | Codex | ☐ |
| TDD-4 | Medium | Codex | ☑ |
| TDD-5 | Medium | Codex | ☑ |
| TDD-6 | Medium | Codex | ☐ (do after CA-2/FE-1) |
| SEC-4 | Medium | Mistral | ✅ |
| SEC-5 | Medium | Mistral | ✅ |
| SEC-6 | Medium | Mistral | ✅ |
| DOC-1 | Low | Mistral | ✅ |
| LIQ-1 | Low | Mistral | ✅ |
| LIQ-2 | Low | Mistral | ✅ |
| CC-2 | Low | Mistral | ✅ |
| CC-3 | Low | Mistral | ✅ |
