package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Game(
        UUID id,
        String name,
        GameType gameType,
        CalculationMethod calculationMethod,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int NAME_MAX_LENGTH = 120;

    public Game {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(gameType, "gameType must not be null");
        Objects.requireNonNull(calculationMethod, "calculationMethod must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        name = name == null ? "" : name.trim();
        description = description == null ? "" : description.trim();
        if (name.isBlank()) throw new DomainValidationException("Game name is required");
        if (name.length() > NAME_MAX_LENGTH) throw new DomainValidationException("Game name must be at most 120 characters");
    }

    public static Game create(String name, GameType gameType, CalculationMethod calculationMethod, String description, Instant now) {
        return new Game(UUID.randomUUID(), name, gameType, calculationMethod, description, now, now);
    }

    public static Game rehydrate(UUID id, String name, GameType gameType, CalculationMethod calculationMethod,
                                 String description, Instant createdAt, Instant updatedAt) {
        return new Game(id, name, gameType, calculationMethod, description, createdAt, updatedAt);
    }

    public Game update(String name, GameType gameType, CalculationMethod calculationMethod, String description, Instant now) {
        return new Game(id, name, gameType, calculationMethod, description, createdAt, now);
    }
}
