package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Item(
        UUID id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int TITLE_MAX_LENGTH = 120;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;

    public Item {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        title = normalizeTitle(title);
        description = normalizeDescription(description);
        validate(title, description);
    }

    public static Item create(String title, String description, Instant now) {
        return new Item(UUID.randomUUID(), title, description, now, now);
    }

    public static Item rehydrate(UUID id, String title, String description, Instant createdAt, Instant updatedAt) {
        return new Item(id, title, description, createdAt, updatedAt);
    }

    public Item update(String title, String description, Instant now) {
        return new Item(id, title, description, createdAt, now);
    }

    private static String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
    }

    private static String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    private static void validate(String title, String description) {
        if (title.isBlank()) {
            throw new DomainValidationException("Title is required");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new DomainValidationException("Title must be at most 120 characters");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException("Description must be at most 1000 characters");
        }
    }
}
