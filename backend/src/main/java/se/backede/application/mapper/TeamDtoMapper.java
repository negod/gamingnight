package se.backede.application.mapper;

import se.backede.application.dto.TeamResponse;
import se.backede.domain.model.Team;

public final class TeamDtoMapper {

    private TeamDtoMapper() {
    }

    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.id(),
                team.name(),
                team.playerIds(),
                team.createdAt(),
                team.updatedAt()
        );
    }
}
