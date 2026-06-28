package se.backede.infrastructure.persistence;

import se.backede.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserJpaMapper {

    UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.username(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.playerId(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    User toDomain(UserEntity entity) {
        return User.rehydrate(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getPlayerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
