package se.backede.infrastructure.persistence;

import se.backede.domain.model.Competition;
import se.backede.domain.repository.CompetitionRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Competition save(Competition competition) {
        return mapper.toDomain(repository.save(mapper.toEntity(competition)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Competition> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Competition> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
