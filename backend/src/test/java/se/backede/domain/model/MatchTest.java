package se.backede.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MatchTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final UUID COMPETITION_ID = UUID.randomUUID();
    private static final UUID GAME_ID = UUID.randomUUID();
    private static final UUID HOME_TEAM = UUID.randomUUID();
    private static final UUID AWAY_TEAM = UUID.randomUUID();

    @Test
    void createHasEmptyResults() {
        var match = Match.create(COMPETITION_ID, GAME_ID, HOME_TEAM, AWAY_TEAM, NOW);

        assertThat(match.results()).isEmpty();
        assertThat(match.isCompleted()).isFalse();
        assertThat(match.competitionId()).isEqualTo(COMPETITION_ID);
        assertThat(match.gameId()).isEqualTo(GAME_ID);
        assertThat(match.homeTeamId()).isEqualTo(HOME_TEAM);
        assertThat(match.awayTeamId()).isEqualTo(AWAY_TEAM);
    }

    @Test
    void withResultsReplacesResults() {
        var match = Match.create(COMPETITION_ID, GAME_ID, HOME_TEAM, AWAY_TEAM, NOW);
        var player = UUID.randomUUID();
        var results = List.of(new PlayerResult(player, HOME_TEAM, 42.0));
        var later = NOW.plusSeconds(60);

        var updated = match.withResults(results, later);

        assertThat(updated.results()).hasSize(1);
        assertThat(updated.results().get(0).value()).isEqualTo(42.0);
        assertThat(updated.updatedAt()).isEqualTo(later);
        assertThat(updated.id()).isEqualTo(match.id());
    }

    @Test
    void isCompletedWhenResultsPresent() {
        var match = Match.create(COMPETITION_ID, GAME_ID, HOME_TEAM, AWAY_TEAM, NOW);
        var player = UUID.randomUUID();
        var withResults = match.withResults(List.of(new PlayerResult(player, HOME_TEAM, 10.0)), NOW);

        assertThat(match.isCompleted()).isFalse();
        assertThat(withResults.isCompleted()).isTrue();
    }

    @Test
    void requiresNonNullCompetitionId() {
        assertThatNullPointerException().isThrownBy(
                () -> Match.create(null, GAME_ID, HOME_TEAM, AWAY_TEAM, NOW));
    }

    @Test
    void resultsAreImmutable() {
        var match = Match.create(COMPETITION_ID, GAME_ID, HOME_TEAM, AWAY_TEAM, NOW);

        assertThat(match.results()).isUnmodifiable();
    }
}
