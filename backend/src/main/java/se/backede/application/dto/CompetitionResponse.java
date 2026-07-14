package se.backede.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompetitionResponse(
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
    public CompetitionResponse(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                               List<UUID> gameIds, List<UUID> teamIds, Instant createdAt, Instant updatedAt) {
        this(id, name, date, singleMatch, false, started, gameIds, teamIds, List.of(), createdAt, updatedAt);
    }

    public CompetitionResponse(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                               List<UUID> gameIds, List<UUID> teamIds, List<UUID> registeredPlayerIds,
                               Instant createdAt, Instant updatedAt) {
        this(id, name, date, singleMatch, false, started, gameIds, teamIds, registeredPlayerIds, createdAt, updatedAt);
    }
}
