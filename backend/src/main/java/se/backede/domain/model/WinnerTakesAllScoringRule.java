package se.backede.domain.model;

public record WinnerTakesAllScoringRule(
        ScoringRuleType type,
        int pointsToWinner
) implements ScoringRule {

    public static WinnerTakesAllScoringRule of(int pointsToWinner) {
        return new WinnerTakesAllScoringRule(ScoringRuleType.WINNER_TAKES_ALL, pointsToWinner);
    }
}
