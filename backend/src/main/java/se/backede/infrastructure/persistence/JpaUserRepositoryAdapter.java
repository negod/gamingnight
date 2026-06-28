package se.backede.infrastructure.persistence;

import se.backede.domain.model.User;
import se.backede.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserJpaMapper mapper;

    public JpaUserRepositoryAdapter(SpringDataUserRepository repository, UserJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return repository.findByUsernameIgnoreCase(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return repository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id) {
        return repository.existsByUsernameIgnoreCaseAndIdNot(username, id);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public boolean existsByPlayerId(UUID playerId) {
        return repository.existsByPlayerId(playerId);
    }

    @Override
    public boolean existsByPlayerIdAndIdNot(UUID playerId, UUID id) {
        return repository.existsByPlayerIdAndIdNot(playerId, id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
