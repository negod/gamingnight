package se.backede.infrastructure.persistence;

import se.backede.domain.model.TeamName;
import se.backede.domain.repository.TeamNameRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JpaTeamNameRepositoryAdapter implements TeamNameRepositoryPort {

    private final SpringDataTeamNameRepository repository;

    public JpaTeamNameRepositoryAdapter(SpringDataTeamNameRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamName> findAll() {
        return repository.findAll().stream()
                .map(entity -> new TeamName(entity.getName()))
                .toList();
    }
}
