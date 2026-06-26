package se.backede.application.dto;

import java.util.UUID;

public record PlayerResultResponse(
        UUID playerId,
        UUID teamId,
        double value
) {
}
