package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createsTeamWithTrimmedNameAndPlayers() {
        var playerId = UUID.randomUUID();

        var team = Team.create(" Alpha ", List.of(playerId), NOW);

        assertThat(team.id()).isNotNull();
        assertThat(team.name()).isEqualTo("Alpha");
        assertThat(team.playerIds()).containsExactly(playerId);
        assertThat(team.createdAt()).isEqualTo(NOW);
        assertThat(team.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void treatsNullPlayersAsEmpty() {
        var team = Team.create("Alpha", null, NOW);

        assertThat(team.playerIds()).isEmpty();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Team.create(" ", List.of(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Team name is required");
    }

    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> Team.create(null, List.of(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Team name is required");
    }

    @Test
    void rejectsTooLongName() {
        var name = "a".repeat(121);

        assertThatThrownBy(() -> Team.create(name, List.of(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Team name must be at most 120 characters");
    }

    @Test
    void updatesNamePlayersAndUpdatedTimestamp() {
        var firstPlayerId = UUID.randomUUID();
        var secondPlayerId = UUID.randomUUID();
        var team = Team.create("Alpha", List.of(firstPlayerId), NOW);
        var later = NOW.plusSeconds(60);

        var updated = team.update(" Beta ", List.of(secondPlayerId), later);

        assertThat(updated.id()).isEqualTo(team.id());
        assertThat(updated.name()).isEqualTo("Beta");
        assertThat(updated.playerIds()).containsExactly(secondPlayerId);
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.updatedAt()).isEqualTo(later);
    }
}
