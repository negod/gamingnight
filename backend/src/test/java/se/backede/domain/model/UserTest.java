package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createsUserWithTrimmedUsername() {
        var playerId = UUID.randomUUID();

        var user = User.create(" admin ", null, "hash", UserRole.ADMIN, playerId, NOW);

        assertThat(user.username()).isEqualTo("admin");
        assertThat(user.email()).isNull();
        assertThat(user.role()).isEqualTo(UserRole.ADMIN);
        assertThat(user.playerId()).isEqualTo(playerId);
        assertThat(user.createdAt()).isEqualTo(NOW);
        assertThat(user.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void normalisesEmailToLowerCase() {
        var user = User.create("admin", "Admin@Example.COM", "hash", UserRole.ADMIN, UUID.randomUUID(), NOW);

        assertThat(user.email()).isEqualTo("admin@example.com");
    }

    @Test
    void treatsBlankEmailAsNull() {
        var user = User.create("admin", "   ", "hash", UserRole.USER, UUID.randomUUID(), NOW);

        assertThat(user.email()).isNull();
    }

    @Test
    void rejectsInvalidEmailFormat() {
        assertThatThrownBy(() -> User.create("admin", "not-an-email", "hash", UserRole.USER, UUID.randomUUID(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email format is invalid");
    }

    @Test
    void rejectsTooLongEmail() {
        var longEmail = "a".repeat(315) + "@b.com"; // 321 chars, over the 320 limit
        assertThatThrownBy(() -> User.create("admin", longEmail, "hash", UserRole.USER, UUID.randomUUID(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Email must be at most 320 characters");
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> User.create(" ", null, "hash", UserRole.USER, UUID.randomUUID(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Username is required");
    }

    @Test
    void rejectsTooLongUsername() {
        assertThatThrownBy(() -> User.create("a".repeat(121), null, "hash", UserRole.USER, UUID.randomUUID(), NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Username must be at most 120 characters");
    }

    @Test
    void rejectsMissingRole() {
        assertThatThrownBy(() -> User.create("admin", null, "hash", null, UUID.randomUUID(), NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("role must not be null");
    }

    @Test
    void rejectsMissingPlayer() {
        assertThatThrownBy(() -> User.create("admin", null, "hash", UserRole.ADMIN, null, NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("playerId must not be null");
    }
}
