package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createsGameWithAllFields() {
        var game = Game.create("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "Standard bowling rules", NOW);

        assertThat(game.id()).isNotNull();
        assertThat(game.name()).isEqualTo("Bowling");
        assertThat(game.gameType()).isEqualTo(GameType.SCORE_BASED);
        assertThat(game.calculationMethod()).isEqualTo(CalculationMethod.SUM);
        assertThat(game.description()).isEqualTo("Standard bowling rules");
        assertThat(game.createdAt()).isEqualTo(NOW);
        assertThat(game.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void treatsNullDescriptionAsEmpty() {
        var game = Game.create("Darts", GameType.TIME_BASED, CalculationMethod.AVERAGE, null, NOW);

        assertThat(game.description()).isEqualTo("");
    }

    @Test
    void rejectsMissingName() {
        assertThatThrownBy(() -> Game.create("  ", GameType.SCORE_BASED, CalculationMethod.SUM, "", NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Game name is required");
    }

    @Test
    void rejectsTooLongName() {
        var name = "a".repeat(121);

        assertThatThrownBy(() -> Game.create(name, GameType.SCORE_BASED, CalculationMethod.SUM, "", NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Game name must be at most 120 characters");
    }

    @Test
    void updatesMutableFieldsAndUpdatedAt() {
        var createdAt = Instant.parse("2026-01-01T10:00:00Z");
        var updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        var game = Game.rehydrate(UUID.randomUUID(), "Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "", createdAt, createdAt);

        var updated = game.update("Darts", GameType.TIME_BASED, CalculationMethod.AVERAGE, "New rules", updatedAt);

        assertThat(updated.id()).isEqualTo(game.id());
        assertThat(updated.name()).isEqualTo("Darts");
        assertThat(updated.gameType()).isEqualTo(GameType.TIME_BASED);
        assertThat(updated.calculationMethod()).isEqualTo(CalculationMethod.AVERAGE);
        assertThat(updated.description()).isEqualTo("New rules");
        assertThat(updated.createdAt()).isEqualTo(createdAt);
        assertThat(updated.updatedAt()).isEqualTo(updatedAt);
    }
}
