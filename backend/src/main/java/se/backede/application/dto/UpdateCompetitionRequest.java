package se.backede.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateCompetitionRequest(
        @NotBlank(message = "Competition name is required")
        @Size(max = 120, message = "Competition name must be at most 120 characters")
        String name,

        @NotNull(message = "Date is required")
        LocalDate date,

        boolean singleMatch,

        @NotNull(message = "Game list is required")
        List<UUID> gameIds,

        @NotNull(message = "Team list is required")
        List<UUID> teamIds
) {
}
