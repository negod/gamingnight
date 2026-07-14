package se.backede.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlayerResultInput(
        @NotNull(message = "Player ID is required")
        UUID playerId,

        @NotNull(message = "Team ID is required")
        UUID teamId,

        @NotNull(message = "Result value is required")
        @DecimalMin(value = "-99999.0", message = "Result value must be at least -99999.0")
        @DecimalMax(value = "99999.0", message = "Result value must be at most 99999.0")
        @Digits(integer = 5, fraction = 2, message = "Result value must have at most 5 integer digits and 2 decimal places")
        Double value
) {
}
