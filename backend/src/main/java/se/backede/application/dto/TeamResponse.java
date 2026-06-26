package se.backede.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        List<UUID> playerIds,
        Instant createdAt,
        Instant updatedAt
) {
}
