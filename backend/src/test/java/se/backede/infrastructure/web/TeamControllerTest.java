package se.backede.infrastructure.web;

import se.backede.application.dto.CreateTeamRequest;
import se.backede.application.dto.TeamResponse;
import se.backede.application.dto.UpdateTeamRequest;
import se.backede.application.usecase.TeamUseCaseService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TeamController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeamUseCaseService teamUseCaseService;

    @Test
    void listsTeams() throws Exception {
        var playerId = UUID.randomUUID();
        when(teamUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "Alpha", List.of(playerId))));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alpha"))
                .andExpect(jsonPath("$[0].playerIds[0]").value(playerId.toString()));
    }

    @Test
    void getsTeamById() throws Exception {
        var id = UUID.randomUUID();
        when(teamUseCaseService.getById(id)).thenReturn(response(id, "Alpha", List.of(UUID.randomUUID())));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alpha"));
    }

    @Test
    void returnsNotFoundWhenTeamDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(teamUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("Team not found: " + id));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team not found: " + id));
    }

    @Test
    void createsTeam() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var request = new CreateTeamRequest("Alpha", List.of(playerId));
        when(teamUseCaseService.create(request)).thenReturn(response(id, "Alpha", List.of(playerId)));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alpha"))
                .andExpect(jsonPath("$.playerIds[0]").value(playerId.toString()));
    }

    @Test
    void returnsBadRequestForBlankName() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest("", List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updatesTeam() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var request = new UpdateTeamRequest("Beta", List.of(playerId));
        when(teamUseCaseService.update(id, request)).thenReturn(response(id, "Beta", List.of(playerId)));

        mockMvc.perform(put("/api/teams/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Beta"))
                .andExpect(jsonPath("$.playerIds[0]").value(playerId.toString()));
    }

    @Test
    void deletesTeam() throws Exception {
        mockMvc.perform(delete("/api/teams/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenTeamDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Team not found: " + id)).when(teamUseCaseService).delete(id);

        mockMvc.perform(delete("/api/teams/{id}", id))
                .andExpect(status().isNotFound());
    }

    private static TeamResponse response(UUID id, String name, List<UUID> playerIds) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new TeamResponse(id, name, playerIds, now, now);
    }
}
