package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record User(
        UUID id,
        String username,
        String email,
        String passwordHash,
        UserRole role,
        UUID playerId,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int USERNAME_MAX_LENGTH = 120;
    public static final int EMAIL_MAX_LENGTH = 320;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        username = username == null ? "" : username.trim();
        passwordHash = passwordHash.trim();
        if (username.isBlank()) throw new DomainValidationException("Username is required");
        if (username.length() > USERNAME_MAX_LENGTH) throw new DomainValidationException("Username must be at most 120 characters");
        if (passwordHash.isBlank()) throw new DomainValidationException("Password hash is required");
        if (email != null) {
            email = email.trim().toLowerCase();
            if (email.isBlank()) {
                email = null;
            } else {
                if (email.length() > EMAIL_MAX_LENGTH)
                    throw new DomainValidationException("Email must be at most 320 characters");
                if (!EMAIL_PATTERN.matcher(email).matches())
                    throw new DomainValidationException("Email format is invalid");
            }
        }
    }

    public static User create(String username, String email, String passwordHash, UserRole role, UUID playerId, Instant now) {
        return new User(UUID.randomUUID(), username, email, passwordHash, role, playerId, now, now);
    }

    public static User rehydrate(UUID id, String username, String email, String passwordHash, UserRole role, UUID playerId, Instant createdAt, Instant updatedAt) {
        return new User(id, username, email, passwordHash, role, playerId, createdAt, updatedAt);
    }

    public User update(String username, String email, String passwordHash, UserRole role, UUID playerId, Instant now) {
        return new User(id, username, email, passwordHash, role, playerId, createdAt, now);
    }
}
