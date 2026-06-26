package se.backede.domain.repository;

import se.backede.domain.model.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepositoryPort {

    Match save(Match match);

    Optional<Match> findById(UUID id);

    List<Match> findByCompetitionId(UUID competitionId);

    List<Match> findByCompetitionIdAndGameId(UUID competitionId, UUID gameId);
}
