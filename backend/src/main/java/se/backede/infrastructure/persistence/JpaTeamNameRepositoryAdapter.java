package se.backede.infrastructure.persistence;

import se.backede.domain.model.TeamName;
import se.backede.domain.repository.TeamNameRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaTeamNameRepositoryAdapter implements TeamNameRepositoryPort {

    private final SpringDataTeamNameRepository repository;

    public JpaTeamNameRepositoryAdapter(SpringDataTeamNameRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TeamName> findAll() {
        return repository.findAll().stream()
                .map(entity -> new TeamName(entity.getName()))
                .toList();
    }
}
