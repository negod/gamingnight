package se.backede.application.usecase;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.domain.model.Game;
import se.backede.domain.model.MatchType;
import se.backede.domain.model.ParticipantRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.TieBreakerRule;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerRule;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.service.GameRulesValidator;
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

    private static final ParticipantRule PARTICIPANTS = new ParticipantRule(1, 4, null, false);
    private static final ScoringRule SCORING = WinDrawLossScoringRule.of(3, 1, 0);

    private GameRepositoryPort repository;
    private GameRulesValidator rulesValidator;
    private GameUseCaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(GameRepositoryPort.class);
        rulesValidator = mock(GameRulesValidator.class);
        service = new GameUseCaseService(repository, rulesValidator, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsGame() {
        var captor = ArgumentCaptor.forClass(Game.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(bowlingRequest());

        assertThat(response.name()).isEqualTo("Bowling");
        assertThat(response.matchType()).isEqualTo(MatchType.FREE_FOR_ALL);
        assertThat(response.resultType()).isEqualTo(ResultType.SCORE);
        assertThat(response.isActive()).isTrue();
        assertThat(response.createdAt()).isEqualTo(NOW);
    }

    @Test
    void createInvokesRulesValidator() {
        when(repository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(bowlingRequest());

        verify(rulesValidator).validate(any(Game.class));
    }

    @Test
    void listsGames() {
        var game = bowlingGame();
        when(repository.findAll()).thenReturn(List.of(game));

        var responses = service.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(game.id());
    }

    @Test
    void getsGameById() {
        var game = bowlingGame();
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
        var game = bowlingGame(NOW.minusSeconds(60));
        when(repository.findById(game.id())).thenReturn(Optional.of(game));
        when(repository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updateRequest = new UpdateGameRequest(
                "Darts", "Updated", null, null, true,
                MatchType.PLAYER_VS_PLAYER, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null);
        var response = service.update(game.id(), updateRequest);

        assertThat(response.name()).isEqualTo("Darts");
        assertThat(response.matchType()).isEqualTo(MatchType.PLAYER_VS_PLAYER);
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

    private static CreateGameRequest bowlingRequest() {
        return new CreateGameRequest(
                "Bowling", "Standard bowling rules", "Wii", "Sports",
                MatchType.FREE_FOR_ALL, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null);
    }

    private static Game bowlingGame() {
        return bowlingGame(NOW);
    }

    private static Game bowlingGame(Instant createdAt) {
        return Game.create(
                "Bowling", "", null, null,
                MatchType.FREE_FOR_ALL, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null, createdAt);
    }
}
