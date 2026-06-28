package se.backede.application.dto;

import java.util.UUID;

public record PlayerResultResponse(
        UUID playerId,
        UUID teamId,
        String playerName,
        String teamName,
        double value
) {
}
