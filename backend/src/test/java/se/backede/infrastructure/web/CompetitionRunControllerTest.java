package se.backede.infrastructure.web;

import se.backede.application.dto.AuthenticatedUser;
import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.EnterResultsRequest;
import se.backede.application.dto.MatchResponse;
import se.backede.application.dto.PlayerResultInput;
import se.backede.application.dto.PlayerResultResponse;
import se.backede.application.usecase.CompetitionUseCaseService;
import se.backede.application.usecase.CompetitionRunUseCaseService;
import se.backede.domain.model.UserRole;
import se.backede.infrastructure.security.AuthContext;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CompetitionRunController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class CompetitionRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompetitionRunUseCaseService service;

    @MockBean
    private CompetitionUseCaseService competitionUseCaseService;

    @BeforeEach
    void authenticateAdmin() {
        AuthContext.set(new AuthenticatedUser(UUID.randomUUID(), "admin", UserRole.ADMIN, UUID.randomUUID()));
    }

    @AfterEach
    void clearAuthContext() {
        AuthContext.clear();
    }

    @Test
    void startReturnsStartedCompetition() throws Exception {
        var id = UUID.randomUUID();
        var now = Instant.parse("2026-01-01T10:00:00Z");
        when(service.start(id)).thenReturn(new CompetitionResponse(id, "Cup", LocalDate.now(),
                true, true, List.of(), List.of(), now, now));

        mockMvc.perform(post("/api/competitions/{id}/start", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true));
    }

    @Test
    void startReturns400WhenAlreadyStarted() throws Exception {
        var id = UUID.randomUUID();
        when(service.start(id)).thenThrow(new DomainValidationException("Competition has already been started"));

        mockMvc.perform(post("/api/competitions/{id}/start", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Competition has already been started"));
    }

    @Test
    void startReturns404WhenNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(service.start(id)).thenThrow(new ResourceNotFoundException("Competition not found: " + id));

        mockMvc.perform(post("/api/competitions/{id}/start", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMatchesReturnsMatchList() throws Exception {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var matchId = UUID.randomUUID();
        var now = Instant.parse("2026-01-01T10:00:00Z");
        var match = new MatchResponse(matchId, competitionId, gameId,
                UUID.randomUUID(), "Home", UUID.randomUUID(), "Away",
                false, List.of(), now, now);

        when(service.getMatches(competitionId, gameId)).thenReturn(List.of(match));

        mockMvc.perform(get("/api/competitions/{cid}/games/{gid}/matches", competitionId, gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].homeTeamName").value("Home"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void enterResultsReturnsUpdatedMatch() throws Exception {
        var competitionId = UUID.randomUUID();
        var matchId = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var now = Instant.parse("2026-01-01T10:00:00Z");
        var request = new EnterResultsRequest(List.of(new PlayerResultInput(playerId, teamId, 150.0)));
        var response = new MatchResponse(matchId, competitionId, UUID.randomUUID(),
                teamId, "Home", UUID.randomUUID(), "Away",
                true, List.of(new PlayerResultResponse(playerId, teamId, "Alice", "Home", 150.0)), now, now);

        when(service.enterResults(eq(competitionId), eq(matchId), any())).thenReturn(response);

        mockMvc.perform(put("/api/competitions/{cid}/matches/{mid}/results", competitionId, matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.results[0].value").value(150.0));
    }

    @Test
    void enterResultsReturns400WhenResultsListIsNull() throws Exception {
        var competitionId = UUID.randomUUID();
        var matchId = UUID.randomUUID();

        mockMvc.perform(put("/api/competitions/{cid}/matches/{mid}/results", competitionId, matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"results\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enterResultsReturns400WhenResultValueIsOutOfRange() throws Exception {
        var competitionId = UUID.randomUUID();
        var matchId = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var teamId = UUID.randomUUID();

        mockMvc.perform(put("/api/competitions/{cid}/matches/{mid}/results", competitionId, matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "results": [
                                    {
                                      "playerId": "%s",
                                      "teamId": "%s",
                                      "value": 100000.0
                                    }
                                  ]
                                }
                                """.formatted(playerId, teamId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
