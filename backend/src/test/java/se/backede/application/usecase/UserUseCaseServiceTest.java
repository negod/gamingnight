package se.backede.application.usecase;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.UpdateUserRequest;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;
import se.backede.domain.model.UserRole;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.UserRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private UserRepositoryPort userRepository;
    private PlayerRepositoryPort playerRepository;
    private PasswordService passwordService;
    private UserUseCaseService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        playerRepository = mock(PlayerRepositoryPort.class);
        passwordService = mock(PasswordService.class);
        when(passwordService.hash("secret")).thenReturn("hashed-secret");
        when(passwordService.hash("new-secret")).thenReturn("hashed-new-secret");
        service = new UserUseCaseService(userRepository, playerRepository, passwordService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsUserForPlayer() {
        var player = player("Alice");
        when(playerRepository.existsById(player.id())).thenReturn(true);
        when(playerRepository.findById(player.id())).thenReturn(Optional.of(player));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var captor = ArgumentCaptor.forClass(User.class);

        var response = service.create(new CreateUserRequest("admin", null, "secret", UserRole.ADMIN, player.id()));

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("admin");
        assertThat(captor.getValue().email()).isNull();
        assertThat(captor.getValue().passwordHash()).isEqualTo("hashed-secret");
        assertThat(captor.getValue().role()).isEqualTo(UserRole.ADMIN);
        assertThat(response.playerName()).isEqualTo("Alice");
        assertThat(response.createdAt()).isEqualTo(NOW);
    }

    @Test
    void createStoresEmailWhenProvided() {
        var player = player("Alice");
        when(playerRepository.existsById(player.id())).thenReturn(true);
        when(playerRepository.findById(player.id())).thenReturn(Optional.of(player));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var captor = ArgumentCaptor.forClass(User.class);

        service.create(new CreateUserRequest("admin", "admin@example.com", "secret", UserRole.ADMIN, player.id()));

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("admin@example.com");
    }

    @Test
    void createThrowsWhenUsernameExists() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("admin", null, "secret", UserRole.ADMIN, playerId)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void createThrowsWhenEmailExists() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("admin", "admin@example.com", "secret", UserRole.ADMIN, playerId)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void createThrowsWhenPlayerDoesNotExist() {
        var playerId = UUID.randomUUID();
        when(playerRepository.existsById(playerId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("admin", null, "secret", UserRole.ADMIN, playerId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Player not found: " + playerId);
    }

    @Test
    void createThrowsWhenPlayerAlreadyHasUser() {
        var playerId = UUID.randomUUID();
        when(playerRepository.existsById(playerId)).thenReturn(true);
        when(userRepository.existsByPlayerId(playerId)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("admin", null, "secret", UserRole.ADMIN, playerId)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Player is already tied to a user");
    }

    @Test
    void listsUsersNewestFirst() {
        var oldPlayer = player("Alice");
        var newPlayer = player("Bob");
        var oldUser = User.rehydrate(UUID.randomUUID(), "alice", null, "hash", UserRole.USER, oldPlayer.id(), NOW.minusSeconds(60), NOW.minusSeconds(60));
        var newUser = User.rehydrate(UUID.randomUUID(), "bob", null, "hash", UserRole.ADMIN, newPlayer.id(), NOW, NOW);
        when(userRepository.findAll()).thenReturn(List.of(oldUser, newUser));
        when(playerRepository.findById(oldPlayer.id())).thenReturn(Optional.of(oldPlayer));
        when(playerRepository.findById(newPlayer.id())).thenReturn(Optional.of(newPlayer));

        var responses = service.list();

        assertThat(responses).extracting("username").containsExactly("bob", "alice");
        assertThat(responses).extracting("playerName").containsExactly("Bob", "Alice");
    }

    @Test
    void updatesUser() {
        var oldPlayer = player("Alice");
        var newPlayer = player("Bob");
        var user = User.create("alice", null, "hash", UserRole.USER, oldPlayer.id(), NOW.minusSeconds(60));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(playerRepository.existsById(newPlayer.id())).thenReturn(true);
        when(playerRepository.findById(newPlayer.id())).thenReturn(Optional.of(newPlayer));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(user.id(), new UpdateUserRequest("admin", null, "new-secret", UserRole.ADMIN, newPlayer.id()));

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        assertThat(response.playerName()).isEqualTo("Bob");
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void updateThrowsWhenAnotherUserHasUsername() {
        var player = player("Alice");
        var user = User.create("alice", null, "hash", UserRole.USER, player.id(), NOW);
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("admin", user.id())).thenReturn(true);

        assertThatThrownBy(() -> service.update(user.id(), new UpdateUserRequest("admin", null, null, UserRole.ADMIN, player.id())))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void updateThrowsWhenAnotherUserHasPlayer() {
        var player = player("Alice");
        var user = User.create("alice", null, "hash", UserRole.USER, UUID.randomUUID(), NOW);
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(playerRepository.existsById(player.id())).thenReturn(true);
        when(userRepository.existsByPlayerIdAndIdNot(player.id(), user.id())).thenReturn(true);

        assertThatThrownBy(() -> service.update(user.id(), new UpdateUserRequest("admin", null, null, UserRole.ADMIN, player.id())))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Player is already tied to a user");
    }

    @Test
    void deletesUser() {
        var id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenUserDoesNotExist() {
        var id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: " + id);
    }

    private static Player player(String name) {
        return Player.rehydrate(UUID.randomUUID(), name, NOW, NOW);
    }
}
