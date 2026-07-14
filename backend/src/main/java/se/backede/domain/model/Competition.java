package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Competition(
        UUID id,
        String name,
        LocalDate date,
        boolean singleMatch,
        boolean started,
        List<UUID> gameIds,
        List<UUID> teamIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int NAME_MAX_LENGTH = 120;

    public Competition {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        name = name == null ? "" : name.trim();
        gameIds = gameIds == null ? List.of() : List.copyOf(gameIds);
        teamIds = teamIds == null ? List.of() : List.copyOf(teamIds);
        if (name.isBlank()) throw new DomainValidationException("Competition name is required");
        if (name.length() > NAME_MAX_LENGTH) throw new DomainValidationException("Competition name must be at most 120 characters");
    }

    public static Competition create(String name, LocalDate date, boolean singleMatch, Instant now) {
        return new Competition(UUID.randomUUID(), name, date, singleMatch, false, List.of(), List.of(), now, now);
    }

    public static Competition rehydrate(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                                        List<UUID> gameIds, List<UUID> teamIds, Instant createdAt, Instant updatedAt) {
        return new Competition(id, name, date, singleMatch, started, gameIds, teamIds, createdAt, updatedAt);
    }

    public Competition update(String name, LocalDate date, boolean singleMatch,
                              List<UUID> gameIds, List<UUID> teamIds, Instant now) {
        return new Competition(id, name, date, singleMatch, started, gameIds, teamIds, createdAt, now);
    }

    public Competition withTeams(List<UUID> teamIds, Instant now) {
        return new Competition(id, name, date, singleMatch, started, gameIds, teamIds, createdAt, now);
    }

    public Competition start(Instant now) {
        if (teamIds.size() < 2) throw new DomainValidationException("Competition must have at least 2 teams to start");
        if (gameIds.isEmpty()) throw new DomainValidationException("Competition must have at least 1 game to start");
        return new Competition(id, name, date, singleMatch, true, gameIds, teamIds, createdAt, now);
    }
}
