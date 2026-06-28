package se.backede.infrastructure.security;

import se.backede.application.dto.AuthenticatedUser;

import java.util.Optional;

public final class AuthContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    public static Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static AuthenticatedUser requireUser() {
        return currentUser().orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
