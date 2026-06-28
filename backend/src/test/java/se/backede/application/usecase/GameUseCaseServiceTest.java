package se.backede.application.usecase;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.Game;
import se.backede.domain.model.GameType;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
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

class GameUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private GameRepositoryPort repository;
    private GameUseCaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(GameRepositoryPort.class);
        service = new GameUseCaseService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsGame() {
        var captor = ArgumentCaptor.forClass(Game.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateGameRequest("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "Bowling rules"));

        assertThat(response.name()).isEqualTo("Bowling");
        assertThat(response.gameType()).isEqualTo(GameType.SCORE_BASED);
        assertThat(response.calculationMethod()).isEqualTo(CalculationMethod.SUM);
        assertThat(response.description()).isEqualTo("Bowling rules");
        assertThat(response.createdAt()).isEqualTo(NOW);
    }

    @Test
    void listsGames() {
        var game = Game.create("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "", NOW);
        when(repository.findAll()).thenReturn(List.of(game));

        var responses = service.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(game.id());
    }

    @Test
    void getsGameById() {
        var game = Game.create("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "", NOW);
        when(repository.findById(game.id())).thenReturn(Optional.of(game));

        var response = service.getById(game.id());

        assertThat(response.name()).isEqualTo("Bowling");
    }

    @Test
    void throwsWhenGameDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Game not found: " + id);
    }

    @Test
    void updatesGame() {
        var game = Game.create("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "", NOW.minusSeconds(60));
        when(repository.findById(game.id())).thenReturn(Optional.of(game));
        when(repository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(game.id(), new UpdateGameRequest("Darts", GameType.TIME_BASED, CalculationMethod.AVERAGE, "New rules"));

        assertThat(response.name()).isEqualTo("Darts");
        assertThat(response.gameType()).isEqualTo(GameType.TIME_BASED);
        assertThat(response.calculationMethod()).isEqualTo(CalculationMethod.AVERAGE);
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void deletesGame() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenGameDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Game not found: " + id);
    }
}
