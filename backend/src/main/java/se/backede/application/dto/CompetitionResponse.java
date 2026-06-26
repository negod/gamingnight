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
        boolean started,
        List<UUID> gameIds,
        List<UUID> teamIds,
        Instant createdAt,
        Instant updatedAt
) {
}
