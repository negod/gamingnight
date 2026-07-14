package se.backede.application.usecase;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.UpdateCurrentUserRequest;
import se.backede.application.dto.UpdateUserRequest;
import se.backede.application.dto.UserResponse;
import se.backede.application.mapper.UserDtoMapper;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.UserRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class UserUseCaseService {

    private final UserRepositoryPort userRepository;
    private final PlayerRepositoryPort playerRepository;
    private final PasswordService passwordService;
    private final Clock clock;

    public UserUseCaseService(UserRepositoryPort userRepository,
                              PlayerRepositoryPort playerRepository,
                              PasswordService passwordService,
                              Clock clock) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.passwordService = passwordService;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        validateUsernameAvailable(request.username());
        validateEmailAvailable(request.email());
        var player = resolvePlayerForCreate(request);
        var user = User.create(request.username(), request.email(), passwordService.hash(request.password()), request.role(), player.id(), now());
        return UserDtoMapper.toResponse(userRepository.save(user), player);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> userNotFound(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(UUID id, UpdateUserRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> userNotFound(id));
        validateUsernameAvailableForUpdate(request.username(), id);
        validateEmailAvailableForUpdate(request.email(), id);
        validatePlayerAvailableForUpdate(request.playerId(), id);
        var player = playerRepository.findById(request.playerId()).orElseThrow(() -> playerNotFound(request.playerId()));
        var passwordHash = request.password() == null || request.password().isBlank()
                ? user.passwordHash()
                : passwordService.hash(request.password());
        var updated = user.update(request.username(), request.email(), passwordHash, request.role(), request.playerId(), now());
        return UserDtoMapper.toResponse(userRepository.save(updated), player);
    }

    public UserResponse getMe(UUID id) {
        return getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserResponse updateMe(UUID id, UpdateCurrentUserRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> userNotFound(id));
        var player = playerRepository.findById(user.playerId()).orElseThrow(() -> playerNotFound(user.playerId()));
        validateEmailAvailableForUpdate(request.email(), id);
        validatePlayerNameAvailableForUpdate(request.playerCallsign(), player.id());

        var updatedPlayer = player.update(request.playerCallsign(), now());
        var updatedUser = user.update(user.username(), request.email(), user.passwordHash(), user.role(), user.playerId(), now());

        var savedPlayer = playerRepository.save(updatedPlayer);
        var savedUser = userRepository.save(updatedUser);
        return UserDtoMapper.toResponse(savedUser, savedPlayer);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw userNotFound(id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        var player = playerRepository.findById(user.playerId()).orElseThrow(() -> playerNotFound(user.playerId()));
        return UserDtoMapper.toResponse(user, player);
    }

    private void validateUsernameAvailable(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DomainValidationException("Username already exists");
        }
    }

    private void validateUsernameAvailableForUpdate(String username, UUID id) {
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new DomainValidationException("Username already exists");
        }
    }

    private void validateEmailAvailable(String email) {
        if (email != null && !email.isBlank() && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DomainValidationException("Email is already registered");
        }
    }

    private void validateEmailAvailableForUpdate(String email, UUID id) {
        if (email != null && !email.isBlank() && userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DomainValidationException("Email is already registered");
        }
    }

    private void validatePlayerAvailable(UUID playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw playerNotFound(playerId);
        }
        if (userRepository.existsByPlayerId(playerId)) {
            throw new DomainValidationException("Player is already tied to a user");
        }
    }

    private Player resolvePlayerForCreate(CreateUserRequest request) {
        var hasExistingPlayer = request.playerId() != null;
        var hasNewPlayer = request.playerCallsign() != null && !request.playerCallsign().isBlank();
        if (hasExistingPlayer == hasNewPlayer) {
            throw new DomainValidationException("Choose an existing player or enter a new Player callsign");
        }
        if (hasExistingPlayer) {
            validatePlayerAvailable(request.playerId());
            return playerRepository.findById(request.playerId()).orElseThrow(() -> playerNotFound(request.playerId()));
        }
        var player = Player.create(request.playerCallsign(), now());
        validatePlayerNameAvailable(player.name());
        return playerRepository.save(player);
    }

    private void validatePlayerAvailableForUpdate(UUID playerId, UUID id) {
        if (!playerRepository.existsById(playerId)) {
            throw playerNotFound(playerId);
        }
        if (userRepository.existsByPlayerIdAndIdNot(playerId, id)) {
            throw new DomainValidationException("Player is already tied to a user");
        }
    }

    private void validatePlayerNameAvailableForUpdate(String name, UUID playerId) {
        if (playerRepository.existsByNameIgnoreCaseAndIdNot(name, playerId)) {
            throw new DomainValidationException("Player callsign already exists");
        }
    }

    private void validatePlayerNameAvailable(String name) {
        if (playerRepository.existsByNameIgnoreCase(name)) {
            throw new DomainValidationException("Player callsign already exists");
        }
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static ResourceNotFoundException userNotFound(UUID id) {
        return new ResourceNotFoundException("User not found: " + id);
    }

    private static ResourceNotFoundException playerNotFound(UUID id) {
        return new ResourceNotFoundException("Player not found: " + id);
    }
}
