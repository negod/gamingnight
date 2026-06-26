package se.backede.infrastructure.persistence;

import se.backede.domain.model.Competition;
import se.backede.domain.repository.CompetitionRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCompetitionRepositoryAdapter implements CompetitionRepositoryPort {

    private final SpringDataCompetitionRepository repository;
    private final CompetitionJpaMapper mapper;

    public JpaCompetitionRepositoryAdapter(SpringDataCompetitionRepository repository, CompetitionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Competition save(Competition competition) {
        return mapper.toDomain(repository.save(mapper.toEntity(competition)));
    }

    @Override
    public List<Competition> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Competition> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
