package se.backede.infrastructure.persistence;

import se.backede.domain.model.User;
import se.backede.domain.repository.UserRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return repository.findByUsernameIgnoreCase(username).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameIgnoreCase(String username) {
        return repository.existsByUsernameIgnoreCase(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id) {
        return repository.existsByUsernameIgnoreCaseAndIdNot(username, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPlayerId(UUID playerId) {
        return repository.existsByPlayerId(playerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPlayerIdAndIdNot(UUID playerId, UUID id) {
        return repository.existsByPlayerIdAndIdNot(playerId, id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
