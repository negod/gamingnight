package se.backede.infrastructure.persistence;

import se.backede.domain.model.Match;
import se.backede.domain.repository.MatchRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaMatchRepositoryAdapter implements MatchRepositoryPort {

    private final SpringDataMatchRepository repository;
    private final MatchJpaMapper mapper;

    public JpaMatchRepositoryAdapter(SpringDataMatchRepository repository, MatchJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Match save(Match match) {
        var existing = repository.findById(match.id());
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.getResults().clear();
            entity.getResults().addAll(mapper.toResultEntities(match.results()));
            entity.setUpdatedAt(match.updatedAt());
            return mapper.toDomain(entity);
        }
        return mapper.toDomain(repository.save(mapper.toEntity(match)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Match> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> findByCompetitionId(UUID competitionId) {
        return repository.findByCompetitionId(competitionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> findByCompetitionIdAndGameId(UUID competitionId, UUID gameId) {
        return repository.findByCompetitionIdAndGameId(competitionId, gameId).stream()
                .map(mapper::toDomain).toList();
    }
}
