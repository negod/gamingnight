package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createsPlayerWithTrimmedName() {
        var player = Player.create(" Alice ", NOW);

        assertThat(player.name()).isEqualTo("Alice");
        assertThat(player.createdAt()).isEqualTo(NOW);
        assertThat(player.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Player.create(" ", NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Player name is required");
    }

    @Test
    void updatesNameAndUpdatedTimestamp() {
        var player = Player.create("Alice", NOW);
        var later = NOW.plusSeconds(60);

        var updated = player.update("Bob", later);

        assertThat(updated.name()).isEqualTo("Bob");
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.updatedAt()).isEqualTo(later);
    }
}
