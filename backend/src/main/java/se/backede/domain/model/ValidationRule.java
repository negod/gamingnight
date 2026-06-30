package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

public record ValidationRule(
        Double minValue,
        Double maxValue,
        boolean allowDecimals,
        boolean required
) {
    public ValidationRule {
        if (minValue != null && maxValue != null && minValue > maxValue)
            throw new DomainValidationException("minValue must be <= maxValue");
    }
}
