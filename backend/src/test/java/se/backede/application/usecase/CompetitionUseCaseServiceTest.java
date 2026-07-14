package se.backede.application.usecase;

import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Team;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompetitionUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDate DATE = LocalDate.parse("2026-02-01");

    private CompetitionRepositoryPort competitionRepository;
    private GameRepositoryPort gameRepository;
    private TeamRepositoryPort teamRepository;
    private CompetitionUseCaseService service;

    @BeforeEach
    void setUp() {
        competitionRepository = mock(CompetitionRepositoryPort.class);
        gameRepository = mock(GameRepositoryPort.class);
        teamRepository = mock(TeamRepositoryPort.class);
        service = new CompetitionUseCaseService(
                competitionRepository,
                gameRepository,
                teamRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void createsCompetitionWithOrderedGamesAndTeams() {
        var gameA = UUID.randomUUID();
        var gameB = UUID.randomUUID();
        var teamA = UUID.randomUUID();
        var teamB = UUID.randomUUID();
        allowGames(gameA, gameB);
        allowTeams(teamA, teamB);
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateCompetitionRequest(
                "Cup",
                DATE,
                false,
                true,
                List.of(gameA, gameB),
                List.of(teamA, teamB)
        ));

        assertThat(response.name()).isEqualTo("Cup");
        assertThat(response.date()).isEqualTo(DATE);
        assertThat(response.singleMatch()).isFalse();
        assertThat(response.registrationOpen()).isTrue();
        assertThat(response.started()).isFalse();
        assertThat(response.gameIds()).containsExactly(gameA, gameB);
        assertThat(response.teamIds()).containsExactly(teamA, teamB);
    }

    @Test
    void createThrowsWhenAssignedGameDoesNotExist() {
        var gameId = UUID.randomUUID();
        when(gameRepository.existsById(gameId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateCompetitionRequest(
                "Cup",
                DATE,
                true,
                false,
                List.of(gameId),
                List.of()
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Game not found: " + gameId);
    }

    @Test
    void createThrowsWhenAssignedTeamDoesNotExist() {
        var gameId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        allowGames(gameId);
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateCompetitionRequest(
                "Cup",
                DATE,
                true,
                false,
                List.of(gameId),
                List.of(teamId)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team not found: " + teamId);
    }

    @Test
    void listsCompetitionsByDateNewestFirst() {
        var oldCompetition = competition("Old", DATE.minusDays(1), false);
        var newCompetition = competition("New", DATE, false);
        when(competitionRepository.findAll()).thenReturn(List.of(oldCompetition, newCompetition));

        var responses = service.list();

        assertThat(responses).extracting("name").containsExactly("New", "Old");
    }

    @Test
    void listForPlayerIncludesOpenCompetitionsAndStartedCompetitionsWherePlayerParticipates() {
        var playerId = UUID.randomUUID();
        var unrelatedStarted = competition("Hidden", DATE.plusDays(2), true);
        var openCompetition = competition("Open", DATE.plusDays(1), false, true);
        var closedCompetition = competition("Closed", DATE.plusDays(3), false, false);
        var registeredStarted = competitionWithRegistration("Registered", DATE, true, playerId);
        when(competitionRepository.findAll()).thenReturn(List.of(closedCompetition, unrelatedStarted, openCompetition, registeredStarted));

        var responses = service.listForPlayer(playerId);

        assertThat(responses).extracting("name").containsExactly("Open", "Registered");
    }

    @Test
    void getByIdForPlayerAllowsOpenCompetitionBeforeRegistration() {
        var playerId = UUID.randomUUID();
        var competition = competition("Open", DATE, false, true);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        var response = service.getByIdForPlayer(competition.id(), playerId);

        assertThat(response.id()).isEqualTo(competition.id());
    }

    @Test
    void getByIdForPlayerHidesClosedSetupCompetition() {
        var playerId = UUID.randomUUID();
        var competition = competition("Closed", DATE, false, false);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.getByIdForPlayer(competition.id(), playerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition not found: " + competition.id());
    }

    @Test
    void getByIdForPlayerAllowsStartedRegisteredCompetition() {
        var playerId = UUID.randomUUID();
        var competition = competitionWithRegistration("Registered", DATE, true, playerId);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        var response = service.getByIdForPlayer(competition.id(), playerId);

        assertThat(response.registeredPlayerIds()).containsExactly(playerId);
    }

    @Test
    void getByIdForPlayerAllowsStartedCompetitionWherePlayerHasTeam() {
        var playerId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var competition = Competition.rehydrate(
                UUID.randomUUID(),
                "Started",
                DATE,
                true,
                true,
                List.of(UUID.randomUUID()),
                List.of(teamId),
                NOW.minusSeconds(60),
                NOW.minusSeconds(60)
        );
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(Team.rehydrate(teamId, "Team", List.of(playerId), NOW, NOW)));

        var response = service.getByIdForPlayer(competition.id(), playerId);

        assertThat(response.id()).isEqualTo(competition.id());
    }

    @Test
    void getByIdForPlayerHidesStartedCompetitionWherePlayerDoesNotParticipate() {
        var playerId = UUID.randomUUID();
        var competition = competition("Started", DATE, true);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.getByIdForPlayer(competition.id(), playerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Competition not found: " + competition.id());
    }

    @Test
    void updatesCompetitionBeforeItHasStarted() {
        var competition = competition("Cup", DATE, false);
        var gameId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        allowGames(gameId);
        allowTeams(teamId);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(competition.id(), new UpdateCompetitionRequest(
                "Finals",
                DATE.plusDays(1),
                false,
                true,
                List.of(gameId),
                List.of(teamId)
        ));

        assertThat(response.name()).isEqualTo("Finals");
        assertThat(response.date()).isEqualTo(DATE.plusDays(1));
        assertThat(response.registrationOpen()).isTrue();
        assertThat(response.gameIds()).containsExactly(gameId);
        assertThat(response.teamIds()).containsExactly(teamId);
    }

    @Test
    void updateThrowsWhenCompetitionHasStarted() {
        var competition = competition("Cup", DATE, true);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.update(competition.id(), new UpdateCompetitionRequest(
                "Finals",
                DATE,
                true,
                false,
                List.of(),
                List.of()
        )))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Cannot edit a started competition");
    }

    @Test
    void deletesCompetition() {
        var id = UUID.randomUUID();
        when(competitionRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(competitionRepository).deleteById(id);
    }

    @Test
    void registersPlayerForCompetition() {
        var playerId = UUID.randomUUID();
        var competition = competition("Cup", DATE, false, true);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registerPlayer(competition.id(), playerId);

        assertThat(response.registeredPlayerIds()).containsExactly(playerId);
    }

    @Test
    void unregistersPlayerFromCompetition() {
        var playerId = UUID.randomUUID();
        var competition = competitionWithRegistration("Cup", DATE, false, playerId);
        when(competitionRepository.findById(competition.id())).thenReturn(Optional.of(competition));
        when(competitionRepository.save(any(Competition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.unregisterPlayer(competition.id(), playerId);

        assertThat(response.registeredPlayerIds()).isEmpty();
    }

    private static Competition competition(String name, LocalDate date, boolean started) {
        return competition(name, date, started, false);
    }

    private static Competition competition(String name, LocalDate date, boolean started, boolean registrationOpen) {
        return Competition.rehydrate(
                UUID.randomUUID(),
                name,
                date,
                true,
                registrationOpen,
                started,
                List.of(UUID.randomUUID()),
                List.of(UUID.randomUUID()),
                List.of(),
                NOW.minusSeconds(60),
                NOW.minusSeconds(60)
        );
    }

    private static Competition competitionWithRegistration(String name, LocalDate date, boolean started, UUID playerId) {
        return Competition.rehydrate(
                UUID.randomUUID(),
                name,
                date,
                true,
                started,
                List.of(UUID.randomUUID()),
                List.of(UUID.randomUUID()),
                List.of(playerId),
                NOW.minusSeconds(60),
                NOW.minusSeconds(60)
        );
    }

    private void allowGames(UUID... gameIds) {
        for (UUID gameId : gameIds) {
            when(gameRepository.existsById(gameId)).thenReturn(true);
        }
    }

    private void allowTeams(UUID... teamIds) {
        for (UUID teamId : teamIds) {
            when(teamRepository.existsById(teamId)).thenReturn(true);
        }
    }
}
