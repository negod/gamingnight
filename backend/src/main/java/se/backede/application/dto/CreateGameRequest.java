package se.backede.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.GameType;

public record CreateGameRequest(
        @NotBlank(message = "Game name is required")
        @Size(max = 120, message = "Game name must be at most 120 characters")
        String name,

        @NotNull(message = "Game type is required")
        GameType gameType,

        @NotNull(message = "Calculation method is required")
        CalculationMethod calculationMethod,

        String description
) {
}
