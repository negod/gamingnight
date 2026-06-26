package se.backede.application.mapper;

import se.backede.application.dto.MatchResponse;
import se.backede.application.dto.PlayerResultResponse;
import se.backede.domain.model.Match;

public final class MatchDtoMapper {

    private MatchDtoMapper() {
    }

    public static MatchResponse toResponse(Match match, String homeTeamName, String awayTeamName) {
        var results = match.results().stream()
                .map(r -> new PlayerResultResponse(r.playerId(), r.teamId(), r.value()))
                .toList();
        return new MatchResponse(
                match.id(),
                match.competitionId(),
                match.gameId(),
                match.homeTeamId(),
                homeTeamName,
                match.awayTeamId(),
                awayTeamName,
                match.isCompleted(),
                results,
                match.createdAt(),
                match.updatedAt()
        );
    }
}
