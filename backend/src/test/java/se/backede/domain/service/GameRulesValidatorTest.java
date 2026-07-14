package se.backede.domain.service;

import se.backede.domain.model.Game;
import se.backede.domain.model.MatchType;
import se.backede.domain.model.ParticipantRule;
import se.backede.domain.model.PlacementScoringRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.TieBreakerRule;
import se.backede.domain.model.ValidationRule;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerRule;
import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameRulesValidatorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final ParticipantRule PARTICIPANTS = new ParticipantRule(1, 4, null, false);
    private static final ScoringRule WIN_DRAW_LOSS = WinDrawLossScoringRule.of(3, 1, 0);
    private static final ScoringRule PLACEMENT = PlacementScoringRule.of(Map.of(1, 10, 2, 7));

    private GameRulesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GameRulesValidator();
    }

    @Test
    void rejectsTimePlusHighestValueWins() {
        var game = game(ResultType.TIME, WinnerRule.HIGHEST_VALUE_WINS, WIN_DRAW_LOSS, null);

        assertThatThrownBy(() -> validator.validate(game))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("HIGHEST_VALUE_WINS");
    }

    @Test
    void acceptsTimePlusLowestValueWins() {
        var game = game(ResultType.TIME, WinnerRule.LOWEST_VALUE_WINS, WIN_DRAW_LOSS, null);

        assertThatCode(() -> validator.validate(game)).doesNotThrowAnyException();
    }

    @Test
    void rejectsPlacementResultWithNonPlacementScoring() {
        var game = game(ResultType.PLACEMENT, WinnerRule.FIRST_TO_FINISH_WINS, WIN_DRAW_LOSS, null);

        assertThatThrownBy(() -> validator.validate(game))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("PlacementScoringRule");
    }

    @Test
    void acceptsPlacementResultWithPlacementScoring() {
        var game = game(ResultType.PLACEMENT, WinnerRule.FIRST_TO_FINISH_WINS, PLACEMENT, null);

        assertThatCode(() -> validator.validate(game)).doesNotThrowAnyException();
    }

    @Test
    void rejectsClosestToTargetWithoutValidationRule() {
        var game = game(ResultType.SCORE, WinnerRule.CLOSEST_TO_TARGET, WIN_DRAW_LOSS, null);

        assertThatThrownBy(() -> validator.validate(game))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CLOSEST_TO_TARGET");
    }

    @Test
    void acceptsClosestToTargetWithValidationRule() {
        var validationRule = new ValidationRule(0.0, 100.0, true, true);
        var game = game(ResultType.SCORE, WinnerRule.CLOSEST_TO_TARGET, WIN_DRAW_LOSS, validationRule);

        assertThatCode(() -> validator.validate(game)).doesNotThrowAnyException();
    }

    private static Game game(ResultType resultType, WinnerRule winnerRule, ScoringRule scoringRule,
                              ValidationRule validationRule) {
        return Game.rehydrate(
                UUID.randomUUID(), "Test Game", "", null, null, null, true,
                MatchType.FREE_FOR_ALL, PARTICIPANTS, resultType, winnerRule, scoringRule,
                TieBreakerRule.ALLOW_DRAW, validationRule, null, null, List.of(), NOW, NOW);
    }
}
