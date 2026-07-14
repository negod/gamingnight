package se.backede.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(
        @Email(message = "Email format is invalid")
        @Size(max = 320, message = "Email must be at most 320 characters")
        String email,

        @NotBlank(message = "Player callsign is required")
        @Size(max = 120, message = "Player callsign must be at most 120 characters")
        String playerCallsign
) {
}
