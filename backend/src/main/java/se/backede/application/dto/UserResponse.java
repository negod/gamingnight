package se.backede.application.dto;

import se.backede.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        UserRole role,
        UUID playerId,
        String playerName,
        Instant createdAt,
        Instant updatedAt
) {
}
