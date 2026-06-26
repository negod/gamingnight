package se.backede.domain.repository;

import se.backede.domain.model.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepositoryPort {

    Game save(Game game);

    List<Game> findAll();

    Optional<Game> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
