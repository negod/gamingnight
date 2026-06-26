package se.backede.domain.repository;

import se.backede.domain.model.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepositoryPort {

    Team save(Team team);

    List<Team> findAll();

    Optional<Team> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
