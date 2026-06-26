package se.backede.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Match(
        UUID id,
        UUID competitionId,
        UUID gameId,
        UUID homeTeamId,
        UUID awayTeamId,
        List<PlayerResult> results,
        Instant createdAt,
        Instant updatedAt
) {
    public Match {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(competitionId, "competitionId must not be null");
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(homeTeamId, "homeTeamId must not be null");
        Objects.requireNonNull(awayTeamId, "awayTeamId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        results = results == null ? List.of() : List.copyOf(results);
    }

    public static Match create(UUID competitionId, UUID gameId, UUID homeTeamId, UUID awayTeamId, Instant now) {
        return new Match(UUID.randomUUID(), competitionId, gameId, homeTeamId, awayTeamId, List.of(), now, now);
    }

    public static Match rehydrate(UUID id, UUID competitionId, UUID gameId, UUID homeTeamId, UUID awayTeamId,
                                   List<PlayerResult> results, Instant createdAt, Instant updatedAt) {
        return new Match(id, competitionId, gameId, homeTeamId, awayTeamId, results, createdAt, updatedAt);
    }

    public Match withResults(List<PlayerResult> results, Instant now) {
        return new Match(id, competitionId, gameId, homeTeamId, awayTeamId, results, createdAt, now);
    }

    public boolean isCompleted() {
        return !results.isEmpty();
    }
}
