package se.backede.application.usecase;

import se.backede.application.dto.CreateItemRequest;
import se.backede.application.dto.UpdateItemRequest;
import se.backede.domain.model.Item;
import se.backede.domain.repository.ItemRepositoryPort;
import se.backede.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemUseCaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private ItemRepositoryPort repository;
    private ItemUseCaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepositoryPort.class);
        service = new ItemUseCaseService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsItem() {
        var captor = ArgumentCaptor.forClass(Item.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateItemRequest("Desk", "Standing desk"));

        assertThat(response.title()).isEqualTo("Desk");
        assertThat(response.description()).isEqualTo("Standing desk");
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(captor.getValue().updatedAt()).isEqualTo(NOW);
    }

    @Test
    void listsItems() {
        var item = Item.create("Desk", "Standing desk", NOW);
        when(repository.findAll()).thenReturn(List.of(item));

        var responses = service.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(item.id());
    }

    @Test
    void getsItemById() {
        var item = Item.create("Desk", "Standing desk", NOW);
        when(repository.findById(item.id())).thenReturn(Optional.of(item));

        var response = service.getById(item.id());

        assertThat(response.title()).isEqualTo("Desk");
    }

    @Test
    void throwsWhenItemDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found: " + id);
    }

    @Test
    void updatesItem() {
        var item = Item.create("Old", "Old description", NOW.minusSeconds(60));
        when(repository.findById(item.id())).thenReturn(Optional.of(item));
        when(repository.save(org.mockito.ArgumentMatchers.any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(item.id(), new UpdateItemRequest("New", "New description"));

        assertThat(response.title()).isEqualTo("New");
        assertThat(response.description()).isEqualTo("New description");
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void deletesItem() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenItemDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found: " + id);
    }
}
