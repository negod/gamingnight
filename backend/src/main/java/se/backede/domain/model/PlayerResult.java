package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.util.Objects;
import java.util.UUID;

public record PlayerResult(UUID playerId, UUID teamId, double value) {

    public PlayerResult {
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(teamId, "teamId must not be null");
        if (!Double.isFinite(value)) {
            throw new DomainValidationException("PlayerResult value must be finite");
        }
    }
}
