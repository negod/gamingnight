package se.backede.domain.model;

public record ScoreBasedScoringRule(
        ScoringRuleType type,
        double multiplier,
        ScoreRounding rounding
) implements ScoringRule {

    public static ScoreBasedScoringRule of(double multiplier, ScoreRounding rounding) {
        return new ScoreBasedScoringRule(ScoringRuleType.SCORE_BASED, multiplier, rounding);
    }
}
