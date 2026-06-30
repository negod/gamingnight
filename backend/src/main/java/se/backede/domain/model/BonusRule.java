package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.util.Objects;

public record BonusRule(
        String name,
        BonusCondition condition,
        int bonusPoints
) {
    public BonusRule {
        Objects.requireNonNull(name, "BonusRule name must not be null");
        Objects.requireNonNull(condition, "BonusRule condition must not be null");
        if (name.isBlank()) throw new DomainValidationException("BonusRule name must not be blank");
    }
}
