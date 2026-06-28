package se.backede.application.dto;

import se.backede.domain.model.UserRole;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String username,
        UserRole role,
        UUID playerId
) {
    public boolean admin() {
        return role == UserRole.ADMIN;
    }
}
