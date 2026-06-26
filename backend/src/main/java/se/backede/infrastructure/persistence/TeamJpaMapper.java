package se.backede.infrastructure.persistence;

import se.backede.domain.model.Team;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@Component
public class TeamJpaMapper {

    TeamEntity toEntity(Team team) {
        return new TeamEntity(
                team.id(),
                team.name(),
                new LinkedHashSet<>(team.playerIds()),
                team.createdAt(),
                team.updatedAt()
        );
    }

    Team toDomain(TeamEntity entity) {
        return Team.rehydrate(
                entity.getId(),
                entity.getName(),
                new ArrayList<>(entity.getPlayerIds()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
