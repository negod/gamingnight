package se.backede.infrastructure.web;

import se.backede.application.dto.AuthenticatedUser;
import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.application.usecase.CompetitionUseCaseService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CompetitionController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createsCompetition() throws Exception {
        var id = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var request = new CreateCompetitionRequest("Cup", LocalDate.parse("2026-02-01"), true, List.of(gameId), List.of(teamId));
        when(competitionUseCaseService.create(request)).thenReturn(response(id, "Cup", false, List.of(gameId), List.of(teamId)));

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Cup"))
                .andExpect(jsonPath("$.gameIds[0]").value(gameId.toString()))
                .andExpect(jsonPath("$.teamIds[0]").value(teamId.toString()));
    }

    @Test
    void returnsBadRequestForInvalidCreateRequest() throws Exception {
        var request = new CreateCompetitionRequest("", null, true, List.of(), List.of());

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listsCompetitions() throws Exception {
        when(competitionUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "Cup", false, List.of(), List.of())));

        mockMvc.perform(get("/api/competitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cup"));
    }

    @Test
    void getsCompetitionById() throws Exception {
        var id = UUID.randomUUID();
        when(competitionUseCaseService.getById(id)).thenReturn(response(id, "Cup", false, List.of(), List.of()));

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void returnsNotFoundWhenCompetitionDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(competitionUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("Competition not found: " + id));

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Competition not found: " + id));
    }

    @Test
    void updatesCompetition() throws Exception {
        var id = UUID.randomUUID();
        var request = new UpdateCompetitionRequest("Finals", LocalDate.parse("2026-02-02"), false, List.of(), List.of());
        when(competitionUseCaseService.update(id, request)).thenReturn(response(id, "Finals", false, List.of(), List.of()));

        mockMvc.perform(put("/api/competitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Finals"));
    }

    @Test
    void updateReturnsBadRequestWhenCompetitionHasStarted() throws Exception {
        var id = UUID.randomUUID();
        var request = new UpdateCompetitionRequest("Finals", LocalDate.parse("2026-02-02"), false, List.of(), List.of());
        when(competitionUseCaseService.update(id, request)).thenThrow(new DomainValidationException("Cannot edit a started competition"));

        mockMvc.perform(put("/api/competitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot edit a started competition"));
    }

    @Test
    void deletesCompetition() throws Exception {
        mockMvc.perform(delete("/api/competitions/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenCompetitionDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Competition not found: " + id)).when(competitionUseCaseService).delete(id);

        mockMvc.perform(delete("/api/competitions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void generatesTeams() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var request = new GenerateTeamsRequest(List.of(playerId), 1);
        when(competitionUseCaseService.generateTeams(id, request)).thenReturn(response(id, "Cup", false, List.of(), List.of(teamId)));

        mockMvc.perform(post("/api/competitions/{id}/generate-teams", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamIds[0]").value(teamId.toString()));
    }

    private static CompetitionResponse response(UUID id, String name, boolean started, List<UUID> gameIds, List<UUID> teamIds) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new CompetitionResponse(
                id,
                name,
                LocalDate.parse("2026-02-01"),
                true,
                started,
                gameIds,
                teamIds,
                now,
                now
        );
    }
}
