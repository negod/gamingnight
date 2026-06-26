package se.backede.infrastructure.persistence;

import se.backede.domain.model.Item;
import se.backede.domain.repository.ItemRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaItemRepositoryAdapter implements ItemRepositoryPort {

    private final SpringDataItemRepository repository;
    private final ItemJpaMapper mapper;

    public JpaItemRepositoryAdapter(SpringDataItemRepository repository, ItemJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Item save(Item item) {
        return mapper.toDomain(repository.save(mapper.toEntity(item)));
    }

    @Override
    public List<Item> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Item> findById(UUID id) {
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
