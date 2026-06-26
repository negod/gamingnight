package se.backede.infrastructure.persistence;

import se.backede.domain.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameJpaMapper {

    GameEntity toEntity(Game game) {
        return new GameEntity(game.id(), game.name(), game.gameType(), game.calculationMethod(),
                game.description(), game.createdAt(), game.updatedAt());
    }

    Game toDomain(GameEntity entity) {
        return Game.rehydrate(entity.getId(), entity.getName(), entity.getGameType(),
                entity.getCalculationMethod(), entity.getDescription(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
