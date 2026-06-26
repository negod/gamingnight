package se.backede.infrastructure.persistence;

import se.backede.domain.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerJpaMapper {

    PlayerEntity toEntity(Player player) {
        return new PlayerEntity(player.id(), player.name(), player.createdAt(), player.updatedAt());
    }

    Player toDomain(PlayerEntity entity) {
        return Player.rehydrate(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
