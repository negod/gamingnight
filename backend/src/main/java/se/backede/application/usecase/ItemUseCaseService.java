package se.backede.application.usecase;

import se.backede.application.dto.CreateItemRequest;
import se.backede.application.dto.ItemResponse;
import se.backede.application.dto.UpdateItemRequest;
import se.backede.application.mapper.ItemDtoMapper;
import se.backede.domain.model.Item;
import se.backede.domain.repository.ItemRepositoryPort;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ItemUseCaseService {

    private final ItemRepositoryPort repository;
    private final Clock clock;

    public ItemUseCaseService(ItemRepositoryPort repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public ItemResponse create(CreateItemRequest request) {
        var item = Item.create(request.title(), request.description(), now());
        return ItemDtoMapper.toResponse(repository.save(item));
    }

    public List<ItemResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Item::createdAt).reversed())
                .map(ItemDtoMapper::toResponse)
                .toList();
    }

    public ItemResponse getById(UUID id) {
        return repository.findById(id)
                .map(ItemDtoMapper::toResponse)
                .orElseThrow(() -> itemNotFound(id));
    }

    public ItemResponse update(UUID id, UpdateItemRequest request) {
        var item = repository.findById(id).orElseThrow(() -> itemNotFound(id));
        var updated = item.update(request.title(), request.description(), now());
        return ItemDtoMapper.toResponse(repository.save(updated));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw itemNotFound(id);
        }
        repository.deleteById(id);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static ResourceNotFoundException itemNotFound(UUID id) {
        return new ResourceNotFoundException("Item not found: " + id);
    }
}
