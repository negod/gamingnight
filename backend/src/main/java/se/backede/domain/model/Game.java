package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Game(
        UUID id,
        String name,
        String description,
        String platform,
        String genre,
        boolean isActive,
        MatchType matchType,
        ParticipantRule participantRule,
        ResultType resultType,
        WinnerRule winnerRule,
        ScoringRule scoringRule,
        TieBreakerRule tieBreakerRule,
        ValidationRule validationRule,
        RotationRule rotationRule,
        TimeLimitRule timeLimitRule,
        List<BonusRule> bonusRules,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int NAME_MAX_LENGTH = 120;

    public Game {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(matchType, "matchType must not be null");
        Objects.requireNonNull(participantRule, "participantRule must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(winnerRule, "winnerRule must not be null");
        Objects.requireNonNull(scoringRule, "scoringRule must not be null");
        Objects.requireNonNull(tieBreakerRule, "tieBreakerRule must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        name = name == null ? "" : name.trim();
        description = description == null ? "" : description.trim();
        bonusRules = bonusRules == null ? List.of() : List.copyOf(bonusRules);
        if (name.isBlank()) throw new DomainValidationException("Game name is required");
        if (name.length() > NAME_MAX_LENGTH)
            throw new DomainValidationException("Game name must be at most 120 characters");
    }

    public static Game create(
            String name, String description, String platform, String genre,
            MatchType matchType, ParticipantRule participantRule, ResultType resultType,
            WinnerRule winnerRule, ScoringRule scoringRule, TieBreakerRule tieBreakerRule,
            ValidationRule validationRule, RotationRule rotationRule, TimeLimitRule timeLimitRule,
            List<BonusRule> bonusRules, Instant now) {
        return new Game(UUID.randomUUID(), name, description, platform, genre, true,
                matchType, participantRule, resultType, winnerRule, scoringRule, tieBreakerRule,
                validationRule, rotationRule, timeLimitRule, bonusRules, now, now);
    }

    public static Game rehydrate(
            UUID id, String name, String description, String platform, String genre, boolean isActive,
            MatchType matchType, ParticipantRule participantRule, ResultType resultType,
            WinnerRule winnerRule, ScoringRule scoringRule, TieBreakerRule tieBreakerRule,
            ValidationRule validationRule, RotationRule rotationRule, TimeLimitRule timeLimitRule,
            List<BonusRule> bonusRules, Instant createdAt, Instant updatedAt) {
        return new Game(id, name, description, platform, genre, isActive,
                matchType, participantRule, resultType, winnerRule, scoringRule, tieBreakerRule,
                validationRule, rotationRule, timeLimitRule, bonusRules, createdAt, updatedAt);
    }

    public Game update(
            String name, String description, String platform, String genre, boolean isActive,
            MatchType matchType, ParticipantRule participantRule, ResultType resultType,
            WinnerRule winnerRule, ScoringRule scoringRule, TieBreakerRule tieBreakerRule,
            ValidationRule validationRule, RotationRule rotationRule, TimeLimitRule timeLimitRule,
            List<BonusRule> bonusRules, Instant now) {
        return new Game(id, name, description, platform, genre, isActive,
                matchType, participantRule, resultType, winnerRule, scoringRule, tieBreakerRule,
                validationRule, rotationRule, timeLimitRule, bonusRules, createdAt, now);
    }
}
