package se.backede.infrastructure.persistence;

import se.backede.domain.model.Game;
import se.backede.domain.repository.GameRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaGameRepositoryAdapter implements GameRepositoryPort {

    private final SpringDataGameRepository repository;
    private final GameJpaMapper mapper;

    public JpaGameRepositoryAdapter(SpringDataGameRepository repository, GameJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Game save(Game game) {
        var existing = repository.findById(game.id());
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.setName(game.name());
            entity.setGameType(game.gameType());
            entity.setCalculationMethod(game.calculationMethod());
            entity.setDescription(game.description());
            entity.setUpdatedAt(game.updatedAt());
            return mapper.toDomain(entity);
        }
        return mapper.toDomain(repository.save(mapper.toEntity(game)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
