package se.backede.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateTeamRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 120, message = "Team name must be at most 120 characters")
        String name,

        @NotNull(message = "Player list is required")
        List<UUID> playerIds
) {
}
