package se.backede.application.usecase;

import se.backede.application.dto.CreatePlayerRequest;
import se.backede.application.dto.PlayerResponse;
import se.backede.application.dto.UpdatePlayerRequest;
import se.backede.application.mapper.PlayerDtoMapper;
import se.backede.domain.model.Player;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerUseCaseService {

    private final PlayerRepositoryPort repository;
    private final Clock clock;

    public PlayerUseCaseService(PlayerRepositoryPort repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public PlayerResponse create(CreatePlayerRequest request) {
        validateNameAvailable(request.name());
        var player = Player.create(request.name(), now());
        return PlayerDtoMapper.toResponse(repository.save(player));
    }

    public List<PlayerResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Player::createdAt).reversed())
                .map(PlayerDtoMapper::toResponse)
                .toList();
    }

    public PlayerResponse getById(UUID id) {
        return repository.findById(id)
                .map(PlayerDtoMapper::toResponse)
                .orElseThrow(() -> playerNotFound(id));
    }

    public PlayerResponse update(UUID id, UpdatePlayerRequest request) {
        var player = repository.findById(id).orElseThrow(() -> playerNotFound(id));
        validateNameAvailableForUpdate(request.name(), id);
        var updated = player.update(request.name(), now());
        return PlayerDtoMapper.toResponse(repository.save(updated));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw playerNotFound(id);
        }
        repository.deleteById(id);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private void validateNameAvailable(String name) {
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DomainValidationException("Player callsign already exists");
        }
    }

    private void validateNameAvailableForUpdate(String name, UUID id) {
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DomainValidationException("Player callsign already exists");
        }
    }

    private static ResourceNotFoundException playerNotFound(UUID id) {
        return new ResourceNotFoundException("Player not found: " + id);
    }
}
