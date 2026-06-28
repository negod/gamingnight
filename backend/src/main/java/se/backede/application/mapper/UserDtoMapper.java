package se.backede.application.mapper;

import se.backede.application.dto.UserResponse;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;

public final class UserDtoMapper {

    private UserDtoMapper() {
    }

    public static UserResponse toResponse(User user, Player player) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.email(),
                user.role(),
                user.playerId(),
                player.name(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
