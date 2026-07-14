package se.backede.application.dto;

import se.backede.domain.model.BonusRule;
import se.backede.domain.model.MatchType;
import se.backede.domain.model.ParticipantRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.RotationRule;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.TieBreakerRule;
import se.backede.domain.model.TimeLimitRule;
import se.backede.domain.model.ValidationRule;
import se.backede.domain.model.WinnerRule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String name,
        String description,
        String platform,
        String genre,
        String referenceUrl,
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
}
