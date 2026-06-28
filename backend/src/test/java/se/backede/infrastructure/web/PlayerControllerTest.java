package se.backede.infrastructure.web;

import se.backede.application.dto.CreatePlayerRequest;
import se.backede.application.dto.PlayerResponse;
import se.backede.application.dto.UpdatePlayerRequest;
import se.backede.application.usecase.PlayerUseCaseService;
import se.backede.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest({PlayerController.class, GlobalExceptionHandler.class})
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerUseCaseService playerUseCaseService;

    @Test
    void createsPlayer() throws Exception {
        var id = UUID.randomUUID();
        when(playerUseCaseService.create(new CreatePlayerRequest("Alice")))
                .thenReturn(response(id, "Alice"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePlayerRequest("Alice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void returnsBadRequestForInvalidJsonRequest() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePlayerRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listsPlayers() throws Exception {
        when(playerUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "Alice")));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void getsPlayerById() throws Exception {
        var id = UUID.randomUUID();
        when(playerUseCaseService.getById(id)).thenReturn(response(id, "Alice"));

        mockMvc.perform(get("/api/players/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void returnsNotFoundWhenPlayerDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(playerUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("Player not found: " + id));

        mockMvc.perform(get("/api/players/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Player not found: " + id));
    }

    @Test
    void updatesPlayer() throws Exception {
        var id = UUID.randomUUID();
        when(playerUseCaseService.update(id, new UpdatePlayerRequest("Bob")))
                .thenReturn(response(id, "Bob"));

        mockMvc.perform(put("/api/players/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdatePlayerRequest("Bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void deletesPlayer() throws Exception {
        mockMvc.perform(delete("/api/players/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenPlayerDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Player not found: " + id)).when(playerUseCaseService).delete(id);

        mockMvc.perform(delete("/api/players/{id}", id))
                .andExpect(status().isNotFound());
    }

    private static PlayerResponse response(UUID id, String name) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new PlayerResponse(id, name, now, now);
    }
}
