package se.backede.application.usecase;

import se.backede.application.dto.EnterResultsRequest;
import se.backede.application.dto.PlayerResultInput;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Match;
import se.backede.domain.model.PlayerResult;
import se.backede.domain.model.Team;
import se.backede.domain.model.Player;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.MatchRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompetitionRunUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private CompetitionRepositoryPort competitionRepo;
    private MatchRepositoryPort matchRepo;
    private TeamRepositoryPort teamRepo;
    private PlayerRepositoryPort playerRepo;
    private CompetitionRunUseCaseService service;

    @BeforeEach
    void setUp() {
        competitionRepo = mock(CompetitionRepositoryPort.class);
        matchRepo = mock(MatchRepositoryPort.class);
        teamRepo = mock(TeamRepositoryPort.class);
        playerRepo = mock(PlayerRepositoryPort.class);
        service = new CompetitionRunUseCaseService(competitionRepo, matchRepo, teamRepo, playerRepo,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void startGeneratesRoundRobinMatchesPerGame() {
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var teamC = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var competition = competition(List.of(gameId), List.of(teamA, teamB, teamC), true);

        when(competitionRepo.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.start(competition.id());

        // 3 teams: 3*(3-1)/2 = 3 matches for 1 game in single-match mode
        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues()).hasSize(3);
        assertThat(captor.getAllValues()).allMatch(m -> m.gameId().equals(gameId));
    }

    @Test
    void startDoubleMatchesWhenNotSingleMatch() {
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var competition = competition(List.of(gameId), List.of(teamA, teamB), false);

        when(competitionRepo.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.start(competition.id());

        // 2 teams, not single-match: 2 matches (A vs B and B vs A)
        var captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
    }

    @Test
    void startMarksCompetitionAsStarted() {
        var competition = competition(List.of(UUID.randomUUID()), List.of(UUID.randomUUID(), UUID.randomUUID()), true);

        when(competitionRepo.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.start(competition.id());

        assertThat(response.started()).isTrue();
    }

    @Test
    void startThrowsWhenAlreadyStarted() {
        var competition = startedCompetition();
        when(competitionRepo.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.start(competition.id()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("already been started");
    }

    @Test
    void startThrowsWhenCompetitionNotFound() {
        var id = UUID.randomUUID();
        when(competitionRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void startThrowsWhenFewerThanTwoTeams() {
        var competition = competition(List.of(UUID.randomUUID()), List.of(UUID.randomUUID()), true);
        when(competitionRepo.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.start(competition.id()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("at least 2 teams");
    }

    @Test
    void enterResultsUpdatesMatch() {
        var competitionId = UUID.randomUUID();
        var homeTeamId = UUID.randomUUID();
        var awayTeamId = UUID.randomUUID();
        var match = Match.create(competitionId, UUID.randomUUID(), homeTeamId, awayTeamId, NOW);
        var playerId = UUID.randomUUID();
        var request = new EnterResultsRequest(List.of(new PlayerResultInput(playerId, homeTeamId, 150.0)));

        when(matchRepo.findById(match.id())).thenReturn(Optional.of(match));
        when(matchRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(teamRepo.findById(homeTeamId)).thenReturn(Optional.of(team(homeTeamId, "Home")));
        when(teamRepo.findById(awayTeamId)).thenReturn(Optional.of(team(awayTeamId, "Away")));
        when(playerRepo.findById(playerId)).thenReturn(Optional.of(player(playerId, "Alice")));

        var response = service.enterResults(competitionId, match.id(), request);

        assertThat(response.completed()).isTrue();
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).value()).isEqualTo(150.0);
        assertThat(response.results().get(0).playerName()).isEqualTo("Alice");
        assertThat(response.homeTeamName()).isEqualTo("Home");
    }

    @Test
    void enterResultsThrowsWhenMatchNotFound() {
        var id = UUID.randomUUID();
        when(matchRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enterResults(UUID.randomUUID(), id,
                new EnterResultsRequest(List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Competition competition(List<UUID> gameIds, List<UUID> teamIds, boolean singleMatch) {
        return Competition.rehydrate(UUID.randomUUID(), "Test", LocalDate.now(), singleMatch, false,
                gameIds, teamIds, NOW, NOW);
    }

    private static Competition startedCompetition() {
        return Competition.rehydrate(UUID.randomUUID(), "Test", LocalDate.now(), true, true,
                List.of(UUID.randomUUID()), List.of(UUID.randomUUID(), UUID.randomUUID()), NOW, NOW);
    }

    private static Team team(UUID id, String name) {
        return Team.rehydrate(id, name, List.of(), NOW, NOW);
    }

    private static Player player(UUID id, String name) {
        return Player.rehydrate(id, name, NOW, NOW);
    }
}
