package se.backede.application.dto;

import se.backede.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 120, message = "Username must be at most 120 characters")
        String username,

        @Email(message = "Email format is invalid")
        @Size(max = 320, message = "Email must be at most 320 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters")
        String password,

        @NotNull(message = "Role is required")
        UserRole role,

        UUID playerId,

        @Size(max = 120, message = "Player callsign must be at most 120 characters")
        String playerCallsign
) {
    public CreateUserRequest(String username, String email, String password, UserRole role, UUID playerId) {
        this(username, email, password, role, playerId, null);
    }
}
