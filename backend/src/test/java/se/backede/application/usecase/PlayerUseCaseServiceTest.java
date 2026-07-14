package se.backede.application.usecase;

import se.backede.application.dto.CreatePlayerRequest;
import se.backede.application.dto.UpdatePlayerRequest;
import se.backede.domain.model.Player;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private PlayerRepositoryPort repository;
    private PlayerUseCaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepositoryPort.class);
        service = new PlayerUseCaseService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsPlayer() {
        var captor = ArgumentCaptor.forClass(Player.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreatePlayerRequest("Alice"));

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(captor.getValue().updatedAt()).isEqualTo(NOW);
    }

    @Test
    void createThrowsWhenPlayerCallsignAlreadyExists() {
        when(repository.existsByNameIgnoreCase("Alice")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreatePlayerRequest("Alice")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Player callsign already exists");
    }

    @Test
    void listsPlayersNewestFirst() {
        var oldPlayer = Player.create("Alice", NOW.minusSeconds(60));
        var newPlayer = Player.create("Bob", NOW);
        when(repository.findAll()).thenReturn(List.of(oldPlayer, newPlayer));

        var responses = service.list();

        assertThat(responses).extracting("name").containsExactly("Bob", "Alice");
    }

    @Test
    void getsPlayerById() {
        var player = Player.create("Alice", NOW);
        when(repository.findById(player.id())).thenReturn(Optional.of(player));

        var response = service.getById(player.id());

        assertThat(response.name()).isEqualTo("Alice");
    }

    @Test
    void throwsWhenPlayerDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Player not found: " + id);
    }

    @Test
    void updatesPlayer() {
        var player = Player.create("Alice", NOW.minusSeconds(60));
        when(repository.findById(player.id())).thenReturn(Optional.of(player));
        when(repository.save(org.mockito.ArgumentMatchers.any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(player.id(), new UpdatePlayerRequest("Bob"));

        assertThat(response.name()).isEqualTo("Bob");
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void updateThrowsWhenAnotherPlayerHasCallsign() {
        var player = Player.create("Alice", NOW.minusSeconds(60));
        when(repository.findById(player.id())).thenReturn(Optional.of(player));
        when(repository.existsByNameIgnoreCaseAndIdNot("Bob", player.id())).thenReturn(true);

        assertThatThrownBy(() -> service.update(player.id(), new UpdatePlayerRequest("Bob")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Player callsign already exists");
    }

    @Test
    void deletesPlayer() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenPlayerDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Player not found: " + id);
    }
}
