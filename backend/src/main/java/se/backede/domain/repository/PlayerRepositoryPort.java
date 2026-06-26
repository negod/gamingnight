package se.backede.domain.repository;

import se.backede.domain.model.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepositoryPort {

    Player save(Player player);

    List<Player> findAll();

    Optional<Player> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
