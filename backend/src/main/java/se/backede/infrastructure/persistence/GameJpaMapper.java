package se.backede.infrastructure.persistence;

import se.backede.domain.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameJpaMapper {

    GameEntity toEntity(Game game) {
        return new GameEntity(
                game.id(), game.name(), game.description(), game.platform(), game.genre(), game.referenceUrl(),
                game.isActive(), game.matchType(), game.participantRule(), game.resultType(),
                game.winnerRule(), game.scoringRule(), game.tieBreakerRule(), game.validationRule(),
                game.rotationRule(), game.timeLimitRule(), game.bonusRules(),
                game.createdAt(), game.updatedAt());
    }

    Game toDomain(GameEntity entity) {
        return Game.rehydrate(
                entity.getId(), entity.getName(), entity.getDescription(), entity.getPlatform(),
                entity.getGenre(), entity.getReferenceUrl(), entity.isActive(), entity.getMatchType(), entity.getParticipantRule(),
                entity.getResultType(), entity.getWinnerRule(), entity.getScoringRule(),
                entity.getTieBreakerRule(), entity.getValidationRule(), entity.getRotationRule(),
                entity.getTimeLimitRule(), entity.getBonusRules(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
