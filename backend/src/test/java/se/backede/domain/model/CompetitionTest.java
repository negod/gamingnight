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
        assertThat(competition.registrationOpen()).isFalse();
        assertThat(competition.started()).isFalse();
        assertThat(competition.gameIds()).isEmpty();
        assertThat(competition.teamIds()).isEmpty();
        assertThat(competition.registeredPlayerIds()).isEmpty();
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
                true,
                List.of(gameId),
                List.of(teamId),
                NOW.plusSeconds(2)
        );

        assertThat(updated.name()).isEqualTo("Finals");
        assertThat(updated.date()).isEqualTo(DATE.plusDays(1));
        assertThat(updated.singleMatch()).isFalse();
        assertThat(updated.registrationOpen()).isTrue();
        assertThat(updated.started()).isTrue();
        assertThat(updated.gameIds()).containsExactly(gameId);
        assertThat(updated.teamIds()).containsExactly(teamId);
    }

    @Test
    void registersPlayerBeforeStart() {
        var playerId = UUID.randomUUID();
        var competition = openForRegistration();

        var registered = competition.registerPlayer(playerId, NOW.plusSeconds(1));
        var duplicate = registered.registerPlayer(playerId, NOW.plusSeconds(2));

        assertThat(registered.registeredPlayerIds()).containsExactly(playerId);
        assertThat(registered.updatedAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(duplicate.registeredPlayerIds()).containsExactly(playerId);
    }

    @Test
    void unregistersPlayerBeforeStart() {
        var playerId = UUID.randomUUID();
        var competition = openForRegistration()
                .registerPlayer(playerId, NOW.plusSeconds(1));

        var unregistered = competition.unregisterPlayer(playerId, NOW.plusSeconds(2));

        assertThat(unregistered.registeredPlayerIds()).isEmpty();
        assertThat(unregistered.updatedAt()).isEqualTo(NOW.plusSeconds(2));
    }

    @Test
    void rejectsRegistrationChangesAfterStart() {
        var playerId = UUID.randomUUID();
        var competition = startable(List.of(UUID.randomUUID()), List.of(UUID.randomUUID(), UUID.randomUUID()))
                .start(NOW.plusSeconds(1));

        assertThatThrownBy(() -> competition.registerPlayer(playerId, NOW.plusSeconds(2)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Cannot register for a started competition");
        assertThatThrownBy(() -> competition.unregisterPlayer(playerId, NOW.plusSeconds(2)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Cannot unregister from a started competition");
    }

    @Test
    void rejectsRegistrationWhenRegistrationIsClosed() {
        var competition = Competition.create("Cup", DATE, true, NOW);

        assertThatThrownBy(() -> competition.registerPlayer(UUID.randomUUID(), NOW.plusSeconds(1)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Competition is not open for registration");
    }

    private static Competition startable(List<UUID> gameIds, List<UUID> teamIds) {
        return Competition.rehydrate(UUID.randomUUID(), "Cup", DATE, true, false, gameIds, teamIds, NOW, NOW);
    }

    private static Competition openForRegistration() {
        return Competition.create("Cup", DATE, true, NOW)
                .update("Cup", DATE, true, true, List.of(), List.of(), NOW.plusSeconds(1));
    }
}
