package se.backede.domain.service;

import se.backede.domain.model.WinnerRule;
import se.backede.domain.service.WinnerCalculator.TeamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WinnerCalculatorTest {

    private WinnerCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new WinnerCalculator();
    }

    @Test
    void highestValueWinsReturnsTeamWithMaxScore() {
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var scores = List.of(new TeamScore(teamA, 150.0), new TeamScore(teamB, 200.0));

        var winner = calculator.calculate(scores, WinnerRule.HIGHEST_VALUE_WINS);

        assertThat(winner).contains(teamB);
    }

    @Test
    void lowestValueWinsReturnsTeamWithMinScore() {
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var scores = List.of(new TeamScore(teamA, 45.3), new TeamScore(teamB, 30.1));

        var winner = calculator.calculate(scores, WinnerRule.LOWEST_VALUE_WINS);

        assertThat(winner).contains(teamB);
    }

    @Test
    void manualWinnerReturnsEmpty() {
        var scores = List.of(new TeamScore(UUID.randomUUID(), 100.0));

        var winner = calculator.calculate(scores, WinnerRule.MANUAL_WINNER);

        assertThat(winner).isEmpty();
    }

    @Test
    void emptyScoresReturnsEmpty() {
        var winner = calculator.calculate(List.of(), WinnerRule.HIGHEST_VALUE_WINS);

        assertThat(winner).isEmpty();
    }
}
