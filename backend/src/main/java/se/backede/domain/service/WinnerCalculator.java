package se.backede.domain.service;

import se.backede.domain.model.WinnerRule;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WinnerCalculator {

    public record TeamScore(UUID teamId, double value) {}

    public Optional<UUID> calculate(List<TeamScore> scores, WinnerRule rule) {
        if (scores == null || scores.isEmpty()) return Optional.empty();

        return switch (rule) {
            case HIGHEST_VALUE_WINS, FIRST_TO_FINISH_WINS, MOST_ROUNDS_WON ->
                    scores.stream().max(Comparator.comparingDouble(TeamScore::value)).map(TeamScore::teamId);

            case LOWEST_VALUE_WINS ->
                    scores.stream().min(Comparator.comparingDouble(TeamScore::value)).map(TeamScore::teamId);

            case LAST_REMAINING_WINS ->
                    scores.stream().max(Comparator.comparingDouble(TeamScore::value)).map(TeamScore::teamId);

            case CLOSEST_TO_TARGET -> Optional.empty();

            case MANUAL_WINNER -> Optional.empty();
        };
    }
}
