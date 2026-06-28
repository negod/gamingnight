package se.backede.application.mapper;

import se.backede.application.dto.MatchResponse;
import se.backede.application.dto.PlayerResultResponse;
import se.backede.domain.model.Match;

import java.util.Map;
import java.util.UUID;

public final class MatchDtoMapper {

    private MatchDtoMapper() {
    }

    public static MatchResponse toResponse(Match match, String homeTeamName, String awayTeamName,
                                           Map<UUID, String> playerNames) {
        var teamNameMap = Map.of(match.homeTeamId(), homeTeamName, match.awayTeamId(), awayTeamName);
        var results = match.results().stream()
                .map(r -> new PlayerResultResponse(
                        r.playerId(),
                        r.teamId(),
                        playerNames.getOrDefault(r.playerId(), r.playerId().toString()),
                        teamNameMap.getOrDefault(r.teamId(), r.teamId().toString()),
                        r.value()))
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
