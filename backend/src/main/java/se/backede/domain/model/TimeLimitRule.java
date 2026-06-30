package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

public record TimeLimitRule(
        boolean hasTimeLimit,
        Integer durationMinutes,
        TimeAction actionWhenTimeRunsOut
) {
    public TimeLimitRule {
        if (hasTimeLimit && (durationMinutes == null || durationMinutes <= 0))
            throw new DomainValidationException("durationMinutes must be positive when hasTimeLimit is true");
    }
}
