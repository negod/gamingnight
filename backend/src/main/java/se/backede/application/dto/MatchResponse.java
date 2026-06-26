package se.backede.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID competitionId,
        UUID gameId,
        UUID homeTeamId,
        String homeTeamName,
        UUID awayTeamId,
        String awayTeamName,
        boolean completed,
        List<PlayerResultResponse> results,
        Instant createdAt,
        Instant updatedAt
) {
}
