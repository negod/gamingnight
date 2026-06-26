package se.backede.application.usecase;

import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Game;
import se.backede.domain.model.GameType;
import se.backede.domain.model.Match;
import se.backede.domain.model.Player;
import se.backede.domain.model.PlayerResult;
import se.backede.domain.model.Team;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.repository.MatchRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private CompetitionRepositoryPort competitionRepo;
    private MatchRepositoryPort matchRepo;
    private GameRepositoryPort gameRepo;
    private TeamRepositoryPort teamRepo;
    private PlayerRepositoryPort playerRepo;
    private LeaderboardUseCaseService service;

    @BeforeEach
    void setUp() {
        competitionRepo = mock(CompetitionRepositoryPort.class);
        matchRepo = mock(MatchRepositoryPort.class);
        gameRepo = mock(GameRepositoryPort.class);
        teamRepo = mock(TeamRepositoryPort.class);
        playerRepo = mock(PlayerRepositoryPort.class);
        service = new LeaderboardUseCaseService(competitionRepo, matchRepo, gameRepo, teamRepo, playerRepo);
    }

    @Test
    void gameTeamLeaderboardRanksHighestFirstForScoreBased() {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var game = scoredGame(gameId, CalculationMethod.SUM);
        var playerA = UUID.randomUUID();
        var playerB = UUID.randomUUID();

        var match = matchWithResults(competitionId, gameId, teamA, teamB,
                List.of(new PlayerResult(playerA, teamA, 100.0), new PlayerResult(playerB, teamB, 200.0)));

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(matchRepo.findByCompetitionIdAndGameId(competitionId, gameId)).thenReturn(List.of(match));
        when(teamRepo.findById(teamA)).thenReturn(Optional.of(team(teamA, "Alpha")));
        when(teamRepo.findById(teamB)).thenReturn(Optional.of(team(teamB, "Beta")));

        var response = service.getGameTeamLeaderboard(competitionId, gameId);

        assertThat(response.columnHeader()).isEqualTo("Total Score");
        assertThat(response.rows()).hasSize(2);
        assertThat(response.rows().get(0).teamName()).isEqualTo("Beta");
        assertThat(response.rows().get(0).rank()).isEqualTo(1);
        assertThat(response.rows().get(1).teamName()).isEqualTo("Alpha");
        assertThat(response.rows().get(1).rank()).isEqualTo(2);
    }

    @Test
    void gameTeamLeaderboardRanksLowestFirstForTimeBased() {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var game = timedGame(gameId, CalculationMethod.SUM);
        var playerA = UUID.randomUUID();
        var playerB = UUID.randomUUID();

        var match = matchWithResults(competitionId, gameId, teamA, teamB,
                List.of(new PlayerResult(playerA, teamA, 30.5), new PlayerResult(playerB, teamB, 45.0)));

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(matchRepo.findByCompetitionIdAndGameId(competitionId, gameId)).thenReturn(List.of(match));
        when(teamRepo.findById(teamA)).thenReturn(Optional.of(team(teamA, "Faster")));
        when(teamRepo.findById(teamB)).thenReturn(Optional.of(team(teamB, "Slower")));

        var response = service.getGameTeamLeaderboard(competitionId, gameId);

        assertThat(response.columnHeader()).isEqualTo("Total Time");
        assertThat(response.rows().get(0).teamName()).isEqualTo("Faster");
    }

    @Test
    void gameTeamLeaderboardAverageHeader() {
        var gameId = UUID.randomUUID();
        var game = scoredGame(gameId, CalculationMethod.AVERAGE);
        var competitionId = UUID.randomUUID();

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(matchRepo.findByCompetitionIdAndGameId(competitionId, gameId)).thenReturn(List.of());

        var response = service.getGameTeamLeaderboard(competitionId, gameId);

        assertThat(response.columnHeader()).isEqualTo("Average Score");
    }

    @Test
    void totalTeamLeaderboardAppliesPlacementPoints() {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var competition = competition(competitionId, List.of(gameId), List.of(teamA, teamB));
        var game = scoredGame(gameId, CalculationMethod.SUM);
        var playerA = UUID.randomUUID();
        var playerB = UUID.randomUUID();

        var match = matchWithResults(competitionId, gameId, teamA, teamB,
                List.of(new PlayerResult(playerA, teamA, 300.0), new PlayerResult(playerB, teamB, 100.0)));

        when(competitionRepo.findById(competitionId)).thenReturn(Optional.of(competition));
        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(matchRepo.findByCompetitionIdAndGameId(competitionId, gameId)).thenReturn(List.of(match));
        when(teamRepo.findById(teamA)).thenReturn(Optional.of(team(teamA, "Winner")));
        when(teamRepo.findById(teamB)).thenReturn(Optional.of(team(teamB, "Loser")));

        var rows = service.getTotalTeamLeaderboard(competitionId);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).teamName()).isEqualTo("Winner");
        assertThat(rows.get(0).points()).isEqualTo(100); // 1st place
        assertThat(rows.get(1).points()).isEqualTo(90);  // 2nd place
    }

    @Test
    void tiedTeamsGetSameRank() {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var game = scoredGame(gameId, CalculationMethod.SUM);
        var playerA = UUID.randomUUID();
        var playerB = UUID.randomUUID();

        var match = matchWithResults(competitionId, gameId, teamA, teamB,
                List.of(new PlayerResult(playerA, teamA, 100.0), new PlayerResult(playerB, teamB, 100.0)));

        when(gameRepo.findById(gameId)).thenReturn(Optional.of(game));
        when(matchRepo.findByCompetitionIdAndGameId(competitionId, gameId)).thenReturn(List.of(match));
        when(teamRepo.findById(teamA)).thenReturn(Optional.of(team(teamA, "Alpha")));
        when(teamRepo.findById(teamB)).thenReturn(Optional.of(team(teamB, "Beta")));

        var response = service.getGameTeamLeaderboard(competitionId, gameId);

        assertThat(response.rows()).allMatch(r -> r.rank() == 1);
    }

    private static Game scoredGame(UUID id, CalculationMethod method) {
        return Game.rehydrate(id, "Bowling", GameType.SCORE_BASED, method, "", NOW, NOW);
    }

    private static Game timedGame(UUID id, CalculationMethod method) {
        return Game.rehydrate(id, "Run", GameType.TIME_BASED, method, "", NOW, NOW);
    }

    private static Match matchWithResults(UUID competitionId, UUID gameId, UUID homeTeam, UUID awayTeam,
                                          List<PlayerResult> results) {
        var base = Match.create(competitionId, gameId, homeTeam, awayTeam, NOW);
        return base.withResults(results, NOW);
    }

    private static Team team(UUID id, String name) {
        return Team.rehydrate(id, name, List.of(), NOW, NOW);
    }

    private static Competition competition(UUID id, List<UUID> gameIds, List<UUID> teamIds) {
        return Competition.rehydrate(id, "Test", LocalDate.now(), true, true, gameIds, teamIds, NOW, NOW);
    }
}
