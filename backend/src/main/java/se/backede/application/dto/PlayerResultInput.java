package se.backede.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlayerResultInput(
        @NotNull(message = "Player ID is required")
        UUID playerId,

        @NotNull(message = "Team ID is required")
        UUID teamId,

        double value
) {
}
