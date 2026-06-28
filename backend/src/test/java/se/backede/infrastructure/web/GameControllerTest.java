package se.backede.infrastructure.web;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.GameResponse;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.application.usecase.GameUseCaseService;
import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.GameType;
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

@WebMvcTest({GameController.class, GlobalExceptionHandler.class, HealthController.class})
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameUseCaseService gameUseCaseService;

    @Test
    void createsGame() throws Exception {
        var id = UUID.randomUUID();
        var request = new CreateGameRequest("Bowling", GameType.SCORE_BASED, CalculationMethod.SUM, "Bowling rules");
        when(gameUseCaseService.create(request)).thenReturn(response(id, "Bowling"));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Bowling"));
    }

    @Test
    void returnsBadRequestWhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGameRequest("", GameType.SCORE_BASED, CalculationMethod.SUM, ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void returnsBadRequestWhenGameTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bowling\",\"gameType\":null,\"calculationMethod\":\"SUM\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listsGames() throws Exception {
        when(gameUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "Bowling")));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bowling"));
    }

    @Test
    void getsGameById() throws Exception {
        var id = UUID.randomUUID();
        when(gameUseCaseService.getById(id)).thenReturn(response(id, "Bowling"));

        mockMvc.perform(get("/api/games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void returnsNotFoundWhenGameDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(gameUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("Game not found: " + id));

        mockMvc.perform(get("/api/games/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game not found: " + id));
    }

    @Test
    void updatesGame() throws Exception {
        var id = UUID.randomUUID();
        var request = new UpdateGameRequest("Darts", GameType.TIME_BASED, CalculationMethod.AVERAGE, "Updated rules");
        when(gameUseCaseService.update(id, request)).thenReturn(response(id, "Darts"));

        mockMvc.perform(put("/api/games/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Darts"));
    }

    @Test
    void deletesGame() throws Exception {
        mockMvc.perform(delete("/api/games/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenGameDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Game not found: " + id)).when(gameUseCaseService).delete(id);

        mockMvc.perform(delete("/api/games/{id}", id))
                .andExpect(status().isNotFound());
    }

    private static GameResponse response(UUID id, String name) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new GameResponse(id, name, GameType.SCORE_BASED, CalculationMethod.SUM, "", now, now);
    }
}
