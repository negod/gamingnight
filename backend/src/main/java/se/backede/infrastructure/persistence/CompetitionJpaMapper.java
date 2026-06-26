package se.backede.infrastructure.persistence;

import se.backede.domain.model.Competition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@Component
public class CompetitionJpaMapper {

    CompetitionEntity toEntity(Competition competition) {
        return new CompetitionEntity(
                competition.id(),
                competition.name(),
                competition.date(),
                competition.singleMatch(),
                competition.started(),
                new ArrayList<>(competition.gameIds()),
                new LinkedHashSet<>(competition.teamIds()),
                competition.createdAt(),
                competition.updatedAt()
        );
    }

    Competition toDomain(CompetitionEntity entity) {
        return Competition.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getDate(),
                entity.isSingleMatch(),
                entity.isStarted(),
                new ArrayList<>(entity.getGameIds()),
                new ArrayList<>(entity.getTeamIds()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
