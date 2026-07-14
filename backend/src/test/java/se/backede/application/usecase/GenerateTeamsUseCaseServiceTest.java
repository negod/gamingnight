package se.backede.application.usecase;

import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Team;
import se.backede.domain.model.TeamName;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamNameRepositoryPort;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateTeamsUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDate DATE = LocalDate.parse("2026-02-01");

    private CompetitionRepositoryPort competitionRepository;
    private TeamRepositoryPort teamRepository;
    private PlayerRepositoryPort playerRepository;
    private TeamNameRepositoryPort teamNameRepository;
    private GenerateTeamsUseCaseService service;

    @BeforeEach
    void setUp() {
        competitionRepository = mock(CompetitionRepositoryPort.class);
        teamRepository = mock(TeamRepositoryPort.class);
        playerRepository = mock(PlayerRepositoryPort.class);
        teamNameRepository = mock(TeamNameRepositoryPort.class);
        service = new GenerateTeamsUseCaseService(
                competitionRepository,
                teamRepository,
                playerRepository,
                teamNameRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void generateTeamsCreatesRandomTeamsAndAssignsThemToCompetition() {
        var competition = competition("Cup", DATE, false);
        var playerIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        playerIds.forEach(playerId -> when(playerRepository.existsById(playerId)).thenReturn(true));
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(teamNameRepository.findAll()).thenReturn(List.of(
                new TeamName("Catalog One"),
                new TeamName("Catalog Two"),
                new TeamName("Catalog Three")
        ));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var teamCaptor = ArgumentCaptor.forClass(Team.class);

        var response = service.generateTeams(competition.id(), new GenerateTeamsRequest(playerIds, 2));

        verify(teamRepository, times(2)).save(teamCaptor.capture());
        assertThat(teamCaptor.getAllValues()).extracting(team -> team.playerIds().size())
                .containsExactlyInAnyOrder(2, 3);
        assertThat(teamCaptor.getAllValues()).extracting(Team::name)
                .doesNotHaveDuplicates()
                .isSubsetOf("Catalog One", "Catalog Two", "Catalog Three");
        assertThat(response.teamIds()).hasSize(2);
        assertThat(response.gameIds()).isEqualTo(competition.gameIds());
    }

    @Test
    void generateTeamsDoesNotReuseExistingTeamNames() {
        var competition = competition("Cup", DATE, false);
        var playerIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        playerIds.forEach(playerId -> when(playerRepository.existsById(playerId)).thenReturn(true));
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(teamRepository.findAll()).thenReturn(List.of(team("Used Name")));
        when(teamNameRepository.findAll()).thenReturn(List.of(
                new TeamName("Used Name"),
                new TeamName("Fresh One"),
                new TeamName("Fresh Two")
        ));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var teamCaptor = ArgumentCaptor.forClass(Team.class);

        service.generateTeams(competition.id(), new GenerateTeamsRequest(playerIds, 2));

        verify(teamRepository, times(2)).save(teamCaptor.capture());
        assertThat(teamCaptor.getAllValues()).extracting(Team::name)
                .containsExactlyInAnyOrder("Fresh One", "Fresh Two");
    }

    @Test
    void generateTeamsThrowsWhenNotEnoughUnusedTeamNamesExist() {
        var competition = competition("Cup", DATE, false);
        var playerIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        playerIds.forEach(playerId -> when(playerRepository.existsById(playerId)).thenReturn(true));
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(teamNameRepository.findAll()).thenReturn(List.of(new TeamName("Only Name")));

        assertThatThrownBy(() -> service.generateTeams(competition.id(), new GenerateTeamsRequest(playerIds, 2)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Not enough unused team names available");
    }

    @Test
    void generateTeamsThrowsWhenPlayerDoesNotExist() {
        var competition = competition("Cup", DATE, false);
        var playerId = UUID.randomUUID();
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(playerRepository.existsById(playerId)).thenReturn(false);

        assertThatThrownBy(() -> service.generateTeams(competition.id(), new GenerateTeamsRequest(List.of(playerId), 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Player not found: " + playerId);
    }

    @Test
    void generateTeamsThrowsWhenCompetitionHasStarted() {
        var competition = competition("Cup", DATE, true);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.generateTeams(competition.id(), new GenerateTeamsRequest(List.of(UUID.randomUUID()), 1)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Cannot modify a started competition");
    }

    @Test
    void generateTeamsThrowsWhenCompetitionDoesNotExist() {
        var id = UUID.randomUUID();
        when(competitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateTeams(id, new GenerateTeamsRequest(List.of(UUID.randomUUID()), 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition not found: " + id);
    }

    private static Competition competition(String name, LocalDate date, boolean started) {
        return Competition.rehydrate(
                UUID.randomUUID(),
                name,
                date,
                true,
                started,
                List.of(UUID.randomUUID()),
                List.of(UUID.randomUUID()),
                NOW.minusSeconds(60),
                NOW.minusSeconds(60)
        );
    }

    private static Team team(String name) {
        return Team.rehydrate(
                UUID.randomUUID(),
                name,
                List.of(),
                NOW.minusSeconds(60),
                NOW.minusSeconds(60)
        );
    }
}
