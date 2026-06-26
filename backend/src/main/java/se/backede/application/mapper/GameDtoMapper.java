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
                game.gameType(),
                game.calculationMethod(),
                game.description(),
                game.createdAt(),
                game.updatedAt()
        );
    }
}
