package se.backede.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
        @NotBlank(message = "Player name is required")
        @Size(max = 120, message = "Player name must be at most 120 characters")
        String name
) {
}
