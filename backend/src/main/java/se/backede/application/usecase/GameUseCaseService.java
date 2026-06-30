package se.backede.application.usecase;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.GameResponse;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.application.mapper.GameDtoMapper;
import se.backede.domain.model.Game;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.service.GameRulesValidator;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GameUseCaseService {

    private final GameRepositoryPort repository;
    private final GameRulesValidator rulesValidator;
    private final Clock clock;

    public GameUseCaseService(GameRepositoryPort repository, GameRulesValidator rulesValidator, Clock clock) {
        this.repository = repository;
        this.rulesValidator = rulesValidator;
        this.clock = clock;
    }

    public GameResponse create(CreateGameRequest request) {
        var game = Game.create(
                request.name(), request.description(), request.platform(), request.genre(),
                request.matchType(), request.participantRule(), request.resultType(),
                request.winnerRule(), request.scoringRule(), request.tieBreakerRule(),
                request.validationRule(), request.rotationRule(), request.timeLimitRule(),
                request.bonusRules(), now()
        );
        rulesValidator.validate(game);
        return GameDtoMapper.toResponse(repository.save(game));
    }

    public List<GameResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Game::createdAt).reversed())
                .map(GameDtoMapper::toResponse)
                .toList();
    }

    public GameResponse getById(UUID id) {
        return repository.findById(id)
                .map(GameDtoMapper::toResponse)
                .orElseThrow(() -> gameNotFound(id));
    }

    public GameResponse update(UUID id, UpdateGameRequest request) {
        var game = repository.findById(id).orElseThrow(() -> gameNotFound(id));
        var updated = game.update(
                request.name(), request.description(), request.platform(), request.genre(),
                request.isActive(), request.matchType(), request.participantRule(), request.resultType(),
                request.winnerRule(), request.scoringRule(), request.tieBreakerRule(),
                request.validationRule(), request.rotationRule(), request.timeLimitRule(),
                request.bonusRules(), now()
        );
        rulesValidator.validate(updated);
        return GameDtoMapper.toResponse(repository.save(updated));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw gameNotFound(id);
        }
        repository.deleteById(id);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static ResourceNotFoundException gameNotFound(UUID id) {
        return new ResourceNotFoundException("Game not found: " + id);
    }
}
