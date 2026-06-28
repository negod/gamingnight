package se.backede.application.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {
}
