package se.backede.domain.model;

public record ManualScoringRule(ScoringRuleType type) implements ScoringRule {

    public static ManualScoringRule instance() {
        return new ManualScoringRule(ScoringRuleType.MANUAL);
    }
}
