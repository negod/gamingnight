package se.backede.infrastructure.web;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.GameResponse;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.application.usecase.GameUseCaseService;
import se.backede.domain.model.MatchType;
import se.backede.domain.model.ParticipantRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.TieBreakerRule;
import se.backede.domain.model.WinDrawLossScoringRule;
import se.backede.domain.model.WinnerRule;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.backede.infrastructure.config.SecurityConfig;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({GameController.class, GlobalExceptionHandler.class, HealthController.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameUseCaseService gameUseCaseService;

    private static final ParticipantRule PARTICIPANTS = new ParticipantRule(1, 4, null, false);
    private static final ScoringRule SCORING = WinDrawLossScoringRule.of(3, 1, 0);

    @Test
    void createsGame() throws Exception {
        var id = UUID.randomUUID();
        var request = bowlingRequest();
        when(gameUseCaseService.create(request)).thenReturn(response(id, "Bowling"));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Bowling"))
                .andExpect(jsonPath("$.matchType").value("FREE_FOR_ALL"))
                .andExpect(jsonPath("$.resultType").value("SCORE"))
                .andExpect(jsonPath("$.scoringRule.type").value("WIN_DRAW_LOSS"));
    }

    @Test
    void returnsBadRequestWhenNameIsMissing() throws Exception {
        var request = new CreateGameRequest(
                "", null, null, null, null,
                MatchType.FREE_FOR_ALL, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void returnsBadRequestWhenMatchTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bowling\",\"matchType\":null,\"participantRule\":{\"minPlayersPerTeam\":1,\"maxPlayersPerTeam\":4,\"numberOfTeams\":null,\"allowSubstitutes\":false},\"resultType\":\"SCORE\",\"winnerRule\":\"HIGHEST_VALUE_WINS\",\"scoringRule\":{\"type\":\"WIN_DRAW_LOSS\",\"pointsForWin\":3,\"pointsForDraw\":1,\"pointsForLoss\":0},\"tieBreakerRule\":\"ALLOW_DRAW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void returnsBadRequestWhenDomainValidationFails() throws Exception {
        when(gameUseCaseService.create(any())).thenThrow(
                new DomainValidationException("TIME result type must not use HIGHEST_VALUE_WINS"));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bowlingRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("TIME result type must not use HIGHEST_VALUE_WINS"));
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
        var request = new UpdateGameRequest(
                "Darts", null, null, null, null, true,
                MatchType.PLAYER_VS_PLAYER, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null);
        when(gameUseCaseService.update(eq(id), any())).thenReturn(response(id, "Darts"));

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

    private static CreateGameRequest bowlingRequest() {
        return new CreateGameRequest(
                "Bowling", null, null, null, null,
                MatchType.FREE_FOR_ALL, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, null);
    }

    private static GameResponse response(UUID id, String name) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new GameResponse(id, name, "", null, null, null, true,
                MatchType.FREE_FOR_ALL, PARTICIPANTS, ResultType.SCORE,
                WinnerRule.HIGHEST_VALUE_WINS, SCORING, TieBreakerRule.ALLOW_DRAW,
                null, null, null, List.of(), now, now);
    }
}
