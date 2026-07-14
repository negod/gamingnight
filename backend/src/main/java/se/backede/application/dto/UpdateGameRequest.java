package se.backede.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

import java.util.List;

public record UpdateGameRequest(
        @NotBlank(message = "Game name is required")
        @Size(max = 120, message = "Game name must be at most 120 characters")
        String name,

        String description,
        String platform,
        String genre,

        @Size(max = 2048, message = "Reference URL must be at most 2048 characters")
        String referenceUrl,

        boolean isActive,

        @NotNull(message = "matchType is required")
        MatchType matchType,

        @NotNull(message = "participantRule is required")
        ParticipantRule participantRule,

        @NotNull(message = "resultType is required")
        ResultType resultType,

        @NotNull(message = "winnerRule is required")
        WinnerRule winnerRule,

        @NotNull(message = "scoringRule is required")
        ScoringRule scoringRule,

        @NotNull(message = "tieBreakerRule is required")
        TieBreakerRule tieBreakerRule,

        ValidationRule validationRule,
        RotationRule rotationRule,
        TimeLimitRule timeLimitRule,
        List<BonusRule> bonusRules
) {
}
