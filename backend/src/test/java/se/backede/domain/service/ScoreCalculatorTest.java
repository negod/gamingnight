package se.backede.domain.service;

import se.backede.domain.model.ManualScoringRule;
import se.backede.domain.model.PlacementScoringRule;
import se.backede.domain.model.ScoreBasedScoringRule;
import se.backede.domain.model.ScoreRounding;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerTakesAllScoringRule;
import se.backede.domain.service.WinnerCalculator.TeamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    private ScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ScoreCalculator();
    }

    @Test
    void winDrawLossAssignsWinAndLossPoints() {
        var winner = UUID.randomUUID();
        var loser = UUID.randomUUID();
        var rule = WinDrawLossScoringRule.of(3, 1, 0);
        var scores = List.of(new TeamScore(winner, 200.0), new TeamScore(loser, 100.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result.get(winner)).isEqualTo(3);
        assertThat(result.get(loser)).isEqualTo(0);
    }

    @Test
    void winDrawLossAssignsDrawPoints() {
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var rule = WinDrawLossScoringRule.of(3, 1, 0);
        var scores = List.of(new TeamScore(teamA, 150.0), new TeamScore(teamB, 150.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result.get(teamA)).isEqualTo(1);
        assertThat(result.get(teamB)).isEqualTo(1);
    }

    @Test
    void placementScoringAssignsPointsByRank() {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        var third = UUID.randomUUID();
        var rule = PlacementScoringRule.of(Map.of(1, 10, 2, 7, 3, 5));
        var scores = List.of(
                new TeamScore(first, 300.0),
                new TeamScore(second, 200.0),
                new TeamScore(third, 100.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result.get(first)).isEqualTo(10);
        assertThat(result.get(second)).isEqualTo(7);
        assertThat(result.get(third)).isEqualTo(5);
    }

    @Test
    void scoreBasedAppliesMultiplierAndRounding() {
        var teamA = UUID.randomUUID();
        var rule = ScoreBasedScoringRule.of(0.1, ScoreRounding.FLOOR);
        var scores = List.of(new TeamScore(teamA, 187.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result.get(teamA)).isEqualTo(18); // floor(187 * 0.1) = floor(18.7) = 18
    }

    @Test
    void winnerTakesAllOnlyRewardsWinner() {
        var winner = UUID.randomUUID();
        var loser = UUID.randomUUID();
        var rule = WinnerTakesAllScoringRule.of(5);
        var scores = List.of(new TeamScore(winner, 300.0), new TeamScore(loser, 100.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result.get(winner)).isEqualTo(5);
        assertThat(result.get(loser)).isEqualTo(0);
    }

    @Test
    void manualScoringReturnsEmptyMap() {
        var rule = ManualScoringRule.instance();
        var scores = List.of(new TeamScore(UUID.randomUUID(), 100.0));

        var result = calculator.calculate(scores, rule);

        assertThat(result).isEmpty();
    }
}
