package se.backede.infrastructure.persistence;

import se.backede.domain.model.Match;
import se.backede.domain.model.PlayerResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MatchJpaMapper {

    MatchEntity toEntity(Match match) {
        var entity = new MatchEntity(
                match.id(), match.competitionId(), match.gameId(),
                match.homeTeamId(), match.awayTeamId(),
                match.createdAt(), match.updatedAt()
        );
        entity.getResults().addAll(toResultEntities(match.results()));
        return entity;
    }

    Match toDomain(MatchEntity entity) {
        var results = entity.getResults().stream()
                .map(r -> new PlayerResult(r.getPlayerId(), r.getTeamId(), r.getValue()))
                .toList();
        return Match.rehydrate(
                entity.getId(), entity.getCompetitionId(), entity.getGameId(),
                entity.getHomeTeamId(), entity.getAwayTeamId(),
                results, entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    List<PlayerResultEntity> toResultEntities(List<PlayerResult> results) {
        return results.stream()
                .map(r -> new PlayerResultEntity(UUID.randomUUID(), r.playerId(), r.teamId(), r.value()))
                .toList();
    }
}
