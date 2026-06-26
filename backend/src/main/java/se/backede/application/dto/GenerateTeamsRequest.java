package se.backede.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record GenerateTeamsRequest(
        @NotEmpty(message = "At least one player is required")
        List<UUID> playerIds,

        @Min(value = 1, message = "Team size must be at least 1")
        int teamSize
) {
}
