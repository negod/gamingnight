package se.backede.domain.model;

public record WinDrawLossScoringRule(
        ScoringRuleType type,
        int pointsForWin,
        int pointsForDraw,
        int pointsForLoss
) implements ScoringRule {

    public static WinDrawLossScoringRule of(int pointsForWin, int pointsForDraw, int pointsForLoss) {
        return new WinDrawLossScoringRule(ScoringRuleType.WIN_DRAW_LOSS, pointsForWin, pointsForDraw, pointsForLoss);
    }
}
