package se.backede.domain.model;

import java.util.Map;

public record PlacementScoringRule(
        ScoringRuleType type,
        Map<Integer, Integer> pointsByPlacement
) implements ScoringRule {

    public static PlacementScoringRule of(Map<Integer, Integer> pointsByPlacement) {
        return new PlacementScoringRule(ScoringRuleType.PLACEMENT, Map.copyOf(pointsByPlacement));
    }
}
