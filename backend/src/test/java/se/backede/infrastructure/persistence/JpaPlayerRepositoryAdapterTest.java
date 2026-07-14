package se.backede.infrastructure.persistence;

import se.backede.domain.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaPlayerRepositoryAdapterTest extends PostgreSqlPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private JpaPlayerRepositoryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveFindAllFindByIdAndDeleteRoundTrip() {
        var player = Player.rehydrate(UUID.randomUUID(), "Persistence Alice", NOW, NOW);

        var saved = adapter.save(player);

        assertThat(saved).isEqualTo(player);
        assertThat(adapter.findById(player.id())).contains(player);
        assertThat(adapter.findAll())
                .extracting(Player::id)
                .contains(player.id());

        adapter.deleteById(player.id());

        assertThat(adapter.findById(player.id())).isEmpty();
        assertThat(adapter.existsById(player.id())).isFalse();
    }

    @Test
    void liquibaseAppliesPlayerAndTeamPlayerForeignKeySchema() {
        var missingTeamId = UUID.randomUUID();
        var playerId = UUID.randomUUID();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into team_player_ids (team_id, player_id) values (?, ?)",
                missingTeamId, playerId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
