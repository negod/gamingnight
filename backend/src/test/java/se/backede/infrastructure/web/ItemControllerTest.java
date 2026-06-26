package se.backede.infrastructure.web;

import se.backede.application.dto.CreateItemRequest;
import se.backede.application.dto.ItemResponse;
import se.backede.application.dto.UpdateItemRequest;
import se.backede.application.usecase.ItemUseCaseService;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ItemController.class, GlobalExceptionHandler.class, HealthController.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemUseCaseService itemUseCaseService;

    @Test
    void createsItem() throws Exception {
        var id = UUID.randomUUID();
        when(itemUseCaseService.create(new CreateItemRequest("Desk", "Standing desk")))
                .thenReturn(response(id, "Desk", "Standing desk"));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateItemRequest("Desk", "Standing desk"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Desk"));
    }

    @Test
    void returnsBadRequestForInvalidJsonRequest() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateItemRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listsItems() throws Exception {
        when(itemUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "Desk", "")));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Desk"));
    }

    @Test
    void getsItemById() throws Exception {
        var id = UUID.randomUUID();
        when(itemUseCaseService.getById(id)).thenReturn(response(id, "Desk", ""));

        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void returnsNotFoundWhenItemDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(itemUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("Item not found: " + id));

        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found: " + id));
    }

    @Test
    void updatesItem() throws Exception {
        var id = UUID.randomUUID();
        when(itemUseCaseService.update(id, new UpdateItemRequest("New", "Updated")))
                .thenReturn(response(id, "New", "Updated"));

        mockMvc.perform(put("/api/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemRequest("New", "Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void returnsBadRequestForDomainValidationFailure() throws Exception {
        var id = UUID.randomUUID();
        when(itemUseCaseService.update(id, new UpdateItemRequest("Valid title", "")))
                .thenThrow(new DomainValidationException("Domain validation failed"));

        mockMvc.perform(put("/api/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemRequest("Valid title", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Domain validation failed"));
    }

    @Test
    void deletesItem() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenItemDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Item not found: " + id)).when(itemUseCaseService).delete(id);

        mockMvc.perform(delete("/api/items/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    private static ItemResponse response(UUID id, String title, String description) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new ItemResponse(id, title, description, now, now);
    }
}
