package se.backede.application.mapper;

import se.backede.application.dto.CompetitionResponse;
import se.backede.domain.model.Competition;

public final class CompetitionDtoMapper {

    private CompetitionDtoMapper() {
    }

    public static CompetitionResponse toResponse(Competition competition) {
        return new CompetitionResponse(
                competition.id(),
                competition.name(),
                competition.date(),
                competition.singleMatch(),
                competition.registrationOpen(),
                competition.started(),
                competition.gameIds(),
                competition.teamIds(),
                competition.registeredPlayerIds(),
                competition.createdAt(),
                competition.updatedAt()
        );
    }
}
