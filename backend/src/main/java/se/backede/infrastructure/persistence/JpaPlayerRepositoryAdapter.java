package se.backede.infrastructure.persistence;

import se.backede.domain.model.Player;
import se.backede.domain.repository.PlayerRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPlayerRepositoryAdapter implements PlayerRepositoryPort {

    private final SpringDataPlayerRepository repository;
    private final PlayerJpaMapper mapper;

    public JpaPlayerRepositoryAdapter(SpringDataPlayerRepository repository, PlayerJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Player save(Player player) {
        var existing = repository.findById(player.id());
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.setName(player.name());
            entity.setUpdatedAt(player.updatedAt());
            return mapper.toDomain(entity);
        }
        return mapper.toDomain(repository.save(mapper.toEntity(player)));
    }

    @Override
    public List<Player> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Player> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
