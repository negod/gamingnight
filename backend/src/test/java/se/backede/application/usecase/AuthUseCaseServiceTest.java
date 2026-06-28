package se.backede.application.usecase;

import se.backede.application.dto.LoginRequest;
import se.backede.application.dto.SignupRequest;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;
import se.backede.domain.model.UserRole;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.UserRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private UserRepositoryPort userRepository;
    private PlayerRepositoryPort playerRepository;
    private PasswordService passwordService;
    private TokenService tokenService;
    private AuthUseCaseService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        playerRepository = mock(PlayerRepositoryPort.class);
        passwordService = mock(PasswordService.class);
        tokenService = mock(TokenService.class);
        when(passwordService.hash(any())).thenAnswer(inv -> "hashed-" + inv.getArgument(0));
        when(tokenService.createToken(any())).thenReturn("test-token");
        service = new AuthUseCaseService(userRepository, playerRepository, passwordService, tokenService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void loginReturnsTokenAndUser() {
        var player = player("Alice");
        var user = User.create("alice", "alice@example.com", "hashed-secret", UserRole.USER, player.id(), NOW);
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(passwordService.matches("secret", "hashed-secret")).thenReturn(true);
        when(playerRepository.findById(player.id())).thenReturn(Optional.of(player));

        var response = service.login(new LoginRequest("alice", "secret"));

        assertThat(response.token()).isEqualTo("test-token");
        assertThat(response.user().username()).isEqualTo("alice");
    }

    @Test
    void loginRejectsUnknownUsername() {
        when(userRepository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("unknown", "secret")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loginRejectsWrongPassword() {
        var player = player("Alice");
        var user = User.create("alice", null, "hashed-secret", UserRole.USER, player.id(), NOW);
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(passwordService.matches("wrong", "hashed-secret")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void signupCreatesUserWithUserRoleAndAutoPlayer() {
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.signup(new SignupRequest("alice", "alice@example.com", "password123"));

        assertThat(response.token()).isEqualTo("test-token");
        assertThat(response.user().username()).isEqualTo("alice");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        assertThat(response.user().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void signupRejectsExistingUsername() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(new SignupRequest("alice", "alice@example.com", "password123")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Username is already taken");
    }

    @Test
    void signupRejectsExistingEmail() {
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(new SignupRequest("alice", "alice@example.com", "password123")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email is already registered");
    }

    private static Player player(String name) {
        return Player.rehydrate(UUID.randomUUID(), name, NOW, NOW);
    }
}
