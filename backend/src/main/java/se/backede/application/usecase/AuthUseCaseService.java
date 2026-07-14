package se.backede.application.usecase;

import se.backede.application.dto.AuthenticatedUser;
import se.backede.application.dto.LoginRequest;
import se.backede.application.dto.LoginResponse;
import se.backede.application.dto.SignupRequest;
import se.backede.application.mapper.UserDtoMapper;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;
import se.backede.domain.model.UserRole;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.UserRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuthUseCaseService {

    private final UserRepositoryPort userRepository;
    private final PlayerRepositoryPort playerRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final Clock clock;

    public AuthUseCaseService(UserRepositoryPort userRepository,
                              PlayerRepositoryPort playerRepository,
                              PasswordService passwordService,
                              TokenService tokenService,
                              Clock clock) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new DomainValidationException("Invalid username or password"));
        if (!passwordService.matches(request.password(), user.passwordHash())) {
            throw new DomainValidationException("Invalid username or password");
        }
        var player = playerRepository.findById(user.playerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + user.playerId()));
        var authenticatedUser = new AuthenticatedUser(user.id(), user.username(), user.role(), user.playerId());
        return new LoginResponse(tokenService.createToken(authenticatedUser), UserDtoMapper.toResponse(user, player));
    }

    public LoginResponse signup(SignupRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DomainValidationException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DomainValidationException("Email is already registered");
        }
        if (playerRepository.existsByNameIgnoreCase(request.username())) {
            throw new DomainValidationException("Player callsign already exists");
        }
        var now = Instant.now(clock);
        var player = playerRepository.save(Player.create(request.username(), now));
        var user = userRepository.save(
                User.create(request.username(), request.email(), passwordService.hash(request.password()), UserRole.USER, player.id(), now));
        var authenticatedUser = new AuthenticatedUser(user.id(), user.username(), user.role(), user.playerId());
        return new LoginResponse(tokenService.createToken(authenticatedUser), UserDtoMapper.toResponse(user, player));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
