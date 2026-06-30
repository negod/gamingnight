package se.backede.domain.service;

import se.backede.domain.model.ManualScoringRule;
import se.backede.domain.model.PlacementScoringRule;
import se.backede.domain.model.ScoreBasedScoringRule;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerTakesAllScoringRule;
import se.backede.domain.service.WinnerCalculator.TeamScore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreCalculator {

    public Map<UUID, Integer> calculate(List<TeamScore> scores, ScoringRule rule) {
        if (scores == null || scores.isEmpty()) return Map.of();

        return switch (rule) {
            case WinDrawLossScoringRule r -> calculateWinDrawLoss(scores, r);
            case PlacementScoringRule r -> calculatePlacement(scores, r);
            case ScoreBasedScoringRule r -> calculateScoreBased(scores, r);
            case WinnerTakesAllScoringRule r -> calculateWinnerTakesAll(scores, r);
            case ManualScoringRule ignored -> Map.of();
        };
    }

    private Map<UUID, Integer> calculateWinDrawLoss(List<TeamScore> scores, WinDrawLossScoringRule rule) {
        if (scores.size() < 2) return Map.of();

        double maxValue = scores.stream().mapToDouble(TeamScore::value).max().orElse(0);
        long winnersCount = scores.stream().filter(s -> s.value() == maxValue).count();

        Map<UUID, Integer> result = new HashMap<>();
        for (var score : scores) {
            if (winnersCount > 1 && score.value() == maxValue) {
                result.put(score.teamId(), rule.pointsForDraw());
            } else if (score.value() == maxValue) {
                result.put(score.teamId(), rule.pointsForWin());
            } else {
                result.put(score.teamId(), rule.pointsForLoss());
            }
        }
        return result;
    }

    private Map<UUID, Integer> calculatePlacement(List<TeamScore> scores, PlacementScoringRule rule) {
        var sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.comparingDouble(TeamScore::value).reversed());

        Map<UUID, Integer> result = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            int placement = i + 1;
            int points = rule.pointsByPlacement().getOrDefault(placement, 0);
            result.put(sorted.get(i).teamId(), points);
        }
        return result;
    }

    private Map<UUID, Integer> calculateScoreBased(List<TeamScore> scores, ScoreBasedScoringRule rule) {
        Map<UUID, Integer> result = new HashMap<>();
        for (var score : scores) {
            double raw = score.value() * rule.multiplier();
            int points = switch (rule.rounding()) {
                case NONE -> (int) raw;
                case FLOOR -> (int) Math.floor(raw);
                case CEIL -> (int) Math.ceil(raw);
                case ROUND -> (int) Math.round(raw);
            };
            result.put(score.teamId(), points);
        }
        return result;
    }

    private Map<UUID, Integer> calculateWinnerTakesAll(List<TeamScore> scores, WinnerTakesAllScoringRule rule) {
        double maxValue = scores.stream().mapToDouble(TeamScore::value).max().orElse(0);
        Map<UUID, Integer> result = new HashMap<>();
        for (var score : scores) {
            result.put(score.teamId(), score.value() == maxValue ? rule.pointsToWinner() : 0);
        }
        return result;
    }
}
