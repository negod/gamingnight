package se.backede.application.dto;

import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.GameType;

import java.time.Instant;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String name,
        GameType gameType,
        CalculationMethod calculationMethod,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
