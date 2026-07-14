package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompetitionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDate DATE = LocalDate.parse("2026-02-01");

    @Test
    void createsCompetitionInSetupState() {
        var competition = Competition.create(" Cup ", DATE, true, NOW);

        assertThat(competition.name()).isEqualTo("Cup");
        assertThat(competition.date()).isEqualTo(DATE);
        assertThat(competition.singleMatch()).isTrue();
        assertThat(competition.started()).isFalse();
        assertThat(competition.gameIds()).isEmpty();
        assertThat(competition.teamIds()).isEmpty();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Competition.create(" ", DATE, true, NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Competition name is required");
    }

    @Test
    void startRejectsCompetitionWithNoTeams() {
        var competition = startable(List.of(UUID.randomUUID()), List.of());

        assertThatThrownBy(() -> competition.start(NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("at least 2 teams");
    }

    @Test
    void startRejectsCompetitionWithOneTeam() {
        var competition = startable(List.of(UUID.randomUUID()), List.of(UUID.randomUUID()));

        assertThatThrownBy(() -> competition.start(NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("at least 2 teams");
    }

    @Test
    void startRejectsCompetitionWithNoGames() {
        var competition = startable(List.of(), List.of(UUID.randomUUID(), UUID.randomUUID()));

        assertThatThrownBy(() -> competition.start(NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("at least 1 game");
    }

    @Test
    void startSucceedsWithTwoTeamsAndOneGame() {
        var competition = startable(List.of(UUID.randomUUID()), List.of(UUID.randomUUID(), UUID.randomUUID()));

        var started = competition.start(NOW);

        assertThat(started.started()).isTrue();
    }

    @Test
    void updatesSetupFieldsAndPreservesStartedState() {
        var gameId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var competition = startable(List.of(UUID.randomUUID()), List.of(UUID.randomUUID(), UUID.randomUUID()))
                .start(NOW.plusSeconds(1));

        var updated = competition.update(
                "Finals",
                DATE.plusDays(1),
                false,
                List.of(gameId),
                List.of(teamId),
                NOW.plusSeconds(2)
        );

        assertThat(updated.name()).isEqualTo("Finals");
        assertThat(updated.date()).isEqualTo(DATE.plusDays(1));
        assertThat(updated.singleMatch()).isFalse();
        assertThat(updated.started()).isTrue();
        assertThat(updated.gameIds()).containsExactly(gameId);
        assertThat(updated.teamIds()).containsExactly(teamId);
    }

    private static Competition startable(List<UUID> gameIds, List<UUID> teamIds) {
        return Competition.rehydrate(UUID.randomUUID(), "Cup", DATE, true, false, gameIds, teamIds, NOW, NOW);
    }
}
