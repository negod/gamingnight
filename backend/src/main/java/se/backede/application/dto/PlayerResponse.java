package se.backede.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
