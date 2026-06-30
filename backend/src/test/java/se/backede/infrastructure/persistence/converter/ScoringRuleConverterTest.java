package se.backede.infrastructure.persistence.converter;

import se.backede.domain.model.ManualScoringRule;
import se.backede.domain.model.PlacementScoringRule;
import se.backede.domain.model.ScoreBasedScoringRule;
import se.backede.domain.model.ScoreRounding;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerTakesAllScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringRuleConverterTest {

    private ScoringRuleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ScoringRuleConverter();
    }

    @Test
    void roundTripsWinDrawLoss() {
        var rule = WinDrawLossScoringRule.of(3, 1, 0);

        var result = roundTrip(rule);

        assertThat(result).isInstanceOf(WinDrawLossScoringRule.class);
        var r = (WinDrawLossScoringRule) result;
        assertThat(r.pointsForWin()).isEqualTo(3);
        assertThat(r.pointsForDraw()).isEqualTo(1);
        assertThat(r.pointsForLoss()).isEqualTo(0);
    }

    @Test
    void roundTripsPlacementWithIntegerMapKeys() {
        var rule = PlacementScoringRule.of(Map.of(1, 10, 2, 7, 3, 5));

        var result = roundTrip(rule);

        assertThat(result).isInstanceOf(PlacementScoringRule.class);
        var r = (PlacementScoringRule) result;
        assertThat(r.pointsByPlacement()).containsEntry(1, 10).containsEntry(2, 7).containsEntry(3, 5);
    }

    @Test
    void roundTripsScoreBased() {
        var rule = ScoreBasedScoringRule.of(0.1, ScoreRounding.FLOOR);

        var result = roundTrip(rule);

        assertThat(result).isInstanceOf(ScoreBasedScoringRule.class);
        var r = (ScoreBasedScoringRule) result;
        assertThat(r.multiplier()).isEqualTo(0.1);
        assertThat(r.rounding()).isEqualTo(ScoreRounding.FLOOR);
    }

    @Test
    void roundTripsWinnerTakesAll() {
        var rule = WinnerTakesAllScoringRule.of(5);

        var result = roundTrip(rule);

        assertThat(result).isInstanceOf(WinnerTakesAllScoringRule.class);
        assertThat(((WinnerTakesAllScoringRule) result).pointsToWinner()).isEqualTo(5);
    }

    @Test
    void roundTripsManual() {
        var rule = ManualScoringRule.instance();

        var result = roundTrip(rule);

        assertThat(result).isInstanceOf(ManualScoringRule.class);
    }

    @Test
    void handlesNullGracefully() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    private ScoringRule roundTrip(ScoringRule rule) {
        var json = converter.convertToDatabaseColumn(rule);
        return converter.convertToEntityAttribute(json);
    }
}
