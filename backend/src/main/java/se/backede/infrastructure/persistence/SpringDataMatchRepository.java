package se.backede.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMatchRepository extends JpaRepository<MatchEntity, UUID> {

    List<MatchEntity> findByCompetitionId(UUID competitionId);

    List<MatchEntity> findByCompetitionIdAndGameId(UUID competitionId, UUID gameId);
}
