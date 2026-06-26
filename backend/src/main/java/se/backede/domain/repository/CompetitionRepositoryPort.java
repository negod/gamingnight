package se.backede.domain.repository;

import se.backede.domain.model.Competition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionRepositoryPort {

    Competition save(Competition competition);

    List<Competition> findAll();

    Optional<Competition> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
