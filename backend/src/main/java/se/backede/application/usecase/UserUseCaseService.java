package se.backede.application.usecase;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.UpdateUserRequest;
import se.backede.application.dto.UserResponse;
import se.backede.application.mapper.UserDtoMapper;
import se.backede.domain.model.Player;
import se.backede.domain.model.User;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.UserRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

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

    public UserResponse create(CreateUserRequest request) {
        validateUsernameAvailable(request.username());
        validateEmailAvailable(request.email());
        validatePlayerAvailable(request.playerId());
        var player = playerRepository.findById(request.playerId()).orElseThrow(() -> playerNotFound(request.playerId()));
        var user = User.create(request.username(), request.email(), passwordService.hash(request.password()), request.role(), request.playerId(), now());
        return UserDtoMapper.toResponse(userRepository.save(user), player);
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> userNotFound(id));
    }

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

    private void validatePlayerAvailableForUpdate(UUID playerId, UUID id) {
        if (!playerRepository.existsById(playerId)) {
            throw playerNotFound(playerId);
        }
        if (userRepository.existsByPlayerIdAndIdNot(playerId, id)) {
            throw new DomainValidationException("Player is already tied to a user");
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
