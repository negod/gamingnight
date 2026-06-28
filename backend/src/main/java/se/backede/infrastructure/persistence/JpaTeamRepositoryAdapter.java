package se.backede.infrastructure.persistence;

import se.backede.domain.model.Team;
import se.backede.domain.repository.TeamRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTeamRepositoryAdapter implements TeamRepositoryPort {

    private final SpringDataTeamRepository repository;
    private final TeamJpaMapper mapper;

    public JpaTeamRepositoryAdapter(SpringDataTeamRepository repository, TeamJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Team save(Team team) {
        return mapper.toDomain(repository.save(mapper.toEntity(team)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id) {
        return repository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
