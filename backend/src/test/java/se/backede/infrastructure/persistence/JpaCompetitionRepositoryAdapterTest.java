package se.backede.infrastructure.persistence;

import se.backede.domain.model.Competition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaCompetitionRepositoryAdapterTest extends PostgreSqlPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDate DATE = LocalDate.parse("2026-02-01");

    @Autowired
    private JpaCompetitionRepositoryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveFindAllFindByIdAndDeleteRoundTrip() {
        var gameA = UUID.randomUUID();
        var gameB = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var competition = Competition.rehydrate(
                UUID.randomUUID(), "Persistence Cup", DATE, false, true,
                List.of(gameA, gameB), List.of(teamA, teamB), NOW, NOW);

        var saved = adapter.save(competition);

        assertThat(saved).isEqualTo(competition);
        assertThat(adapter.findById(competition.id())).contains(competition);
        assertThat(adapter.findAll())
                .extracting(Competition::id)
                .contains(competition.id());

        adapter.deleteById(competition.id());

        assertThat(adapter.findById(competition.id())).isEmpty();
        assertThat(adapter.existsById(competition.id())).isFalse();
    }

    @Test
    void deletingCompetitionRemovesOwnedGameAndTeamRows() {
        var competition = Competition.rehydrate(
                UUID.randomUUID(), "Cascade Cup", DATE, true, false,
                List.of(UUID.randomUUID()), List.of(UUID.randomUUID()), NOW, NOW);
        adapter.save(competition);

        assertThat(collectionRowCount("competition_game_ids", competition.id())).isEqualTo(1);
        assertThat(collectionRowCount("competition_team_ids", competition.id())).isEqualTo(1);

        adapter.deleteById(competition.id());

        assertThat(collectionRowCount("competition_game_ids", competition.id())).isZero();
        assertThat(collectionRowCount("competition_team_ids", competition.id())).isZero();
    }

    @Test
    void competitionCollectionTablesRejectRowsForMissingCompetition() {
        var missingCompetitionId = UUID.randomUUID();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into competition_team_ids (competition_id, team_id) values (?, ?)",
                missingCompetitionId, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private int collectionRowCount(String tableName, UUID competitionId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from " + tableName + " where competition_id = ?",
                Integer.class,
                competitionId);
        return count == null ? 0 : count;
    }
}
