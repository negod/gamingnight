package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemTest {

    @Test
    void createsItemWithValidData() {
        var now = Instant.parse("2026-01-01T10:00:00Z");

        var item = Item.create("Keyboard", "Mechanical keyboard", now);

        assertThat(item.id()).isNotNull();
        assertThat(item.title()).isEqualTo("Keyboard");
        assertThat(item.description()).isEqualTo("Mechanical keyboard");
        assertThat(item.createdAt()).isEqualTo(now);
        assertThat(item.updatedAt()).isEqualTo(now);
    }

    @Test
    void rejectsMissingTitle() {
        var now = Instant.parse("2026-01-01T10:00:00Z");

        assertThatThrownBy(() -> Item.create("  ", "Description", now))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Title is required");
    }

    @Test
    void rejectsTooLongTitle() {
        var title = "a".repeat(121);

        assertThatThrownBy(() -> Item.create(title, "Description", Instant.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Title must be at most 120 characters");
    }

    @Test
    void rejectsTooLongDescription() {
        var description = "a".repeat(1001);

        assertThatThrownBy(() -> Item.create("Title", description, Instant.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Description must be at most 1000 characters");
    }

    @Test
    void updatesMutableFieldsAndUpdatedAt() {
        var createdAt = Instant.parse("2026-01-01T10:00:00Z");
        var updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        var item = Item.rehydrate(UUID.randomUUID(), "Old", "Old description", createdAt, createdAt);

        var updated = item.update("New", "New description", updatedAt);

        assertThat(updated.id()).isEqualTo(item.id());
        assertThat(updated.title()).isEqualTo("New");
        assertThat(updated.description()).isEqualTo("New description");
        assertThat(updated.createdAt()).isEqualTo(createdAt);
        assertThat(updated.updatedAt()).isEqualTo(updatedAt);
    }
}
