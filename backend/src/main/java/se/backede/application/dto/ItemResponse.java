package se.backede.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
