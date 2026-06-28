package se.backede.domain.repository;

import se.backede.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(UUID id);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsById(UUID id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByPlayerId(UUID playerId);

    boolean existsByPlayerIdAndIdNot(UUID playerId, UUID id);

    void deleteById(UUID id);
}
