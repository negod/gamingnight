package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Player(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int NAME_MAX_LENGTH = 120;

    public Player {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        name = name == null ? "" : name.trim();
        if (name.isBlank()) throw new DomainValidationException("Player name is required");
        if (name.length() > NAME_MAX_LENGTH) throw new DomainValidationException("Player name must be at most 120 characters");
    }

    public static Player create(String name, Instant now) {
        return new Player(UUID.randomUUID(), name, now, now);
    }

    public static Player rehydrate(UUID id, String name, Instant createdAt, Instant updatedAt) {
        return new Player(id, name, createdAt, updatedAt);
    }

    public Player update(String name, Instant now) {
        return new Player(id, name, createdAt, now);
    }
}
