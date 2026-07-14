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
        boolean registrationOpen,
        boolean started,
        List<UUID> gameIds,
        List<UUID> teamIds,
        List<UUID> registeredPlayerIds,
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
        registeredPlayerIds = registeredPlayerIds == null ? List.of() : List.copyOf(registeredPlayerIds.stream().distinct().toList());
        if (name.isBlank()) throw new DomainValidationException("Competition name is required");
        if (name.length() > NAME_MAX_LENGTH) throw new DomainValidationException("Competition name must be at most 120 characters");
    }

    public static Competition create(String name, LocalDate date, boolean singleMatch, Instant now) {
        return new Competition(UUID.randomUUID(), name, date, singleMatch, false, false, List.of(), List.of(), List.of(), now, now);
    }

    public static Competition rehydrate(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                                        List<UUID> gameIds, List<UUID> teamIds, Instant createdAt, Instant updatedAt) {
        return rehydrate(id, name, date, singleMatch, false, started, gameIds, teamIds, List.of(), createdAt, updatedAt);
    }

    public static Competition rehydrate(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                                        List<UUID> gameIds, List<UUID> teamIds, List<UUID> registeredPlayerIds,
                                        Instant createdAt, Instant updatedAt) {
        return rehydrate(id, name, date, singleMatch, false, started, gameIds, teamIds, registeredPlayerIds, createdAt, updatedAt);
    }

    public static Competition rehydrate(UUID id, String name, LocalDate date, boolean singleMatch, boolean registrationOpen,
                                        boolean started, List<UUID> gameIds, List<UUID> teamIds,
                                        List<UUID> registeredPlayerIds, Instant createdAt, Instant updatedAt) {
        return new Competition(id, name, date, singleMatch, registrationOpen, started, gameIds, teamIds, registeredPlayerIds, createdAt, updatedAt);
    }

    public Competition update(String name, LocalDate date, boolean singleMatch,
                              List<UUID> gameIds, List<UUID> teamIds, Instant now) {
        return update(name, date, singleMatch, registrationOpen, gameIds, teamIds, now);
    }

    public Competition update(String name, LocalDate date, boolean singleMatch, boolean registrationOpen,
                              List<UUID> gameIds, List<UUID> teamIds, Instant now) {
        return new Competition(id, name, date, singleMatch, registrationOpen, started, gameIds, teamIds, registeredPlayerIds, createdAt, now);
    }

    public Competition withTeams(List<UUID> teamIds, Instant now) {
        return new Competition(id, name, date, singleMatch, registrationOpen, started, gameIds, teamIds, registeredPlayerIds, createdAt, now);
    }

    public Competition registerPlayer(UUID playerId, Instant now) {
        Objects.requireNonNull(playerId, "playerId must not be null");
        if (started) throw new DomainValidationException("Cannot register for a started competition");
        if (!registrationOpen) throw new DomainValidationException("Competition is not open for registration");
        if (registeredPlayerIds.contains(playerId)) {
            return this;
        }
        var updatedRegisteredPlayerIds = new java.util.ArrayList<>(registeredPlayerIds);
        updatedRegisteredPlayerIds.add(playerId);
        return new Competition(id, name, date, singleMatch, registrationOpen, started, gameIds, teamIds, updatedRegisteredPlayerIds, createdAt, now);
    }

    public Competition unregisterPlayer(UUID playerId, Instant now) {
        Objects.requireNonNull(playerId, "playerId must not be null");
        if (started) throw new DomainValidationException("Cannot unregister from a started competition");
        if (!registeredPlayerIds.contains(playerId)) {
            return this;
        }
        var updatedRegisteredPlayerIds = registeredPlayerIds.stream()
                .filter(id -> !id.equals(playerId))
                .toList();
        return new Competition(id, name, date, singleMatch, registrationOpen, started, gameIds, teamIds, updatedRegisteredPlayerIds, createdAt, now);
    }

    public Competition start(Instant now) {
        if (teamIds.size() < 2) throw new DomainValidationException("Competition must have at least 2 teams to start");
        if (gameIds.isEmpty()) throw new DomainValidationException("Competition must have at least 1 game to start");
        return new Competition(id, name, date, singleMatch, registrationOpen, true, gameIds, teamIds, registeredPlayerIds, createdAt, now);
    }
}
