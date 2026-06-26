package se.backede.application.mapper;

import se.backede.application.dto.PlayerResponse;
import se.backede.domain.model.Player;

public final class PlayerDtoMapper {

    private PlayerDtoMapper() {
    }

    public static PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.id(),
                player.name(),
                player.createdAt(),
                player.updatedAt()
        );
    }
}
