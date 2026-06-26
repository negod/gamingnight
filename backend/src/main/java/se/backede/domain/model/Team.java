package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Team(
        UUID id,
        String name,
        List<UUID> playerIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int NAME_MAX_LENGTH = 120;

    public Team {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        name = name == null ? "" : name.trim();
        playerIds = playerIds == null ? List.of() : List.copyOf(playerIds);
        if (name.isBlank()) throw new DomainValidationException("Team name is required");
        if (name.length() > NAME_MAX_LENGTH) throw new DomainValidationException("Team name must be at most 120 characters");
    }

    public static Team create(String name, List<UUID> playerIds, Instant now) {
        return new Team(UUID.randomUUID(), name, playerIds, now, now);
    }

    public static Team rehydrate(UUID id, String name, List<UUID> playerIds, Instant createdAt, Instant updatedAt) {
        return new Team(id, name, playerIds, createdAt, updatedAt);
    }

    public Team update(String name, List<UUID> playerIds, Instant now) {
        return new Team(id, name, playerIds, createdAt, now);
    }
}
