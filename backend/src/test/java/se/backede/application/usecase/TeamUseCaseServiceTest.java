package se.backede.application.usecase;

import se.backede.application.dto.CreateTeamRequest;
import se.backede.application.dto.UpdateTeamRequest;
import se.backede.domain.model.Team;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeamUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private TeamRepositoryPort repository;
    private TeamUseCaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(TeamRepositoryPort.class);
        service = new TeamUseCaseService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createThrowsWhenTeamNameAlreadyExists() {
        when(repository.existsByNameIgnoreCase("Team Alpha")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateTeamRequest("Team Alpha", List.of())))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Team name already exists");
    }

    @Test
    void updateThrowsWhenAnotherTeamHasName() {
        var id = UUID.randomUUID();
        var team = Team.rehydrate(id, "Old Name", List.of(), NOW, NOW);
        when(repository.findById(id)).thenReturn(Optional.of(team));
        when(repository.existsByNameIgnoreCaseAndIdNot("Team Alpha", id)).thenReturn(true);

        assertThatThrownBy(() -> service.update(id, new UpdateTeamRequest("Team Alpha", List.of())))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Team name already exists");
    }
}
