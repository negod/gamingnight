package se.backede.domain.repository;

import se.backede.domain.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepositoryPort {

    Item save(Item item);

    List<Item> findAll();

    Optional<Item> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
