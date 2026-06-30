package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private static final ParticipantRule DEFAULT_PARTICIPANTS =
            new ParticipantRule(1, 4, null, false);
    private static final ScoringRule DEFAULT_SCORING =
            WinDrawLossScoringRule.of(3, 1, 0);

    @Test
    void createsGameWithAllRequiredFields() {
        var game = Game.create(
                "Bowling", "Standard bowling rules", "Wii", "Bowling",
                MatchType.FREE_FOR_ALL, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, NOW);

        assertThat(game.id()).isNotNull();
        assertThat(game.name()).isEqualTo("Bowling");
        assertThat(game.description()).isEqualTo("Standard bowling rules");
        assertThat(game.isActive()).isTrue();
        assertThat(game.matchType()).isEqualTo(MatchType.FREE_FOR_ALL);
        assertThat(game.resultType()).isEqualTo(ResultType.SCORE);
        assertThat(game.winnerRule()).isEqualTo(WinnerRule.HIGHEST_VALUE_WINS);
        assertThat(game.tieBreakerRule()).isEqualTo(TieBreakerRule.ALLOW_DRAW);
        assertThat(game.bonusRules()).isEmpty();
        assertThat(game.createdAt()).isEqualTo(NOW);
        assertThat(game.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void treatsNullDescriptionAsEmpty() {
        var game = Game.create(
                "Darts", null, null, null,
                MatchType.PLAYER_VS_PLAYER, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, NOW);

        assertThat(game.description()).isEqualTo("");
    }

    @Test
    void normalisesNullBonusRulesToEmptyList() {
        var game = Game.create(
                "Darts", null, null, null,
                MatchType.PLAYER_VS_PLAYER, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, NOW);

        assertThat(game.bonusRules()).isEmpty();
    }

    @Test
    void rejectsMissingName() {
        assertThatThrownBy(() -> Game.create(
                "  ", null, null, null,
                MatchType.PLAYER_VS_PLAYER, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Game name is required");
    }

    @Test
    void rejectsTooLongName() {
        var name = "a".repeat(121);

        assertThatThrownBy(() -> Game.create(
                name, null, null, null,
                MatchType.PLAYER_VS_PLAYER, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Game name must be at most 120 characters");
    }

    @Test
    void updatesMutableFieldsAndUpdatedAt() {
        var createdAt = Instant.parse("2026-01-01T10:00:00Z");
        var updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        var newScoring = WinnerTakesAllScoringRule.of(5);
        var game = Game.rehydrate(
                UUID.randomUUID(), "Bowling", "", null, null, true,
                MatchType.FREE_FOR_ALL, DEFAULT_PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, DEFAULT_SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, List.of(), createdAt, createdAt);

        var updated = game.update(
                "Darts", "New rules", null, null, false,
                MatchType.PLAYER_VS_PLAYER, DEFAULT_PARTICIPANTS, ResultType.WINNER_ONLY,
                WinnerRule.LAST_REMAINING_WINS, newScoring, TieBreakerRule.EXTRA_ROUND,
                null, null, null, null, updatedAt);

        assertThat(updated.id()).isEqualTo(game.id());
        assertThat(updated.name()).isEqualTo("Darts");
        assertThat(updated.matchType()).isEqualTo(MatchType.PLAYER_VS_PLAYER);
        assertThat(updated.resultType()).isEqualTo(ResultType.WINNER_ONLY);
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.createdAt()).isEqualTo(createdAt);
        assertThat(updated.updatedAt()).isEqualTo(updatedAt);
    }
}
