package se.backede.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 120, message = "Username must be at most 120 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 320, message = "Email must be at most 320 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters")
        String password
) {
}
