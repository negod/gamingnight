package se.backede.application.mapper;

import se.backede.application.dto.GameResponse;
import se.backede.domain.model.Game;

public final class GameDtoMapper {

    private GameDtoMapper() {
    }

    public static GameResponse toResponse(Game game) {
        return new GameResponse(
                game.id(),
                game.name(),
                game.description(),
                game.platform(),
                game.genre(),
                game.referenceUrl(),
                game.isActive(),
                game.matchType(),
                game.participantRule(),
                game.resultType(),
                game.winnerRule(),
                game.scoringRule(),
                game.tieBreakerRule(),
                game.validationRule(),
                game.rotationRule(),
                game.timeLimitRule(),
                game.bonusRules(),
                game.createdAt(),
                game.updatedAt()
        );
    }
}
