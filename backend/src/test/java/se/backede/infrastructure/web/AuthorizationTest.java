package se.backede.infrastructure.web;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.usecase.CompetitionUseCaseService;
import se.backede.application.usecase.TokenService;
import se.backede.infrastructure.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CompetitionController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompetitionUseCaseService competitionUseCaseService;

    @MockBean
    private TokenService tokenService;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/competitions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleOnAdminOnlyEndpointReturns403() throws Exception {
        var request = new CreateCompetitionRequest("Cup", LocalDate.now(), true, List.of(), List.of());

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCanAccessCompetitionList() throws Exception {
        when(competitionUseCaseService.listForPlayer(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/competitions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanCreateCompetition() throws Exception {
        var id = UUID.randomUUID();
        var request = new CreateCompetitionRequest("Cup", LocalDate.now(), true, List.of(), List.of());
        var now = Instant.parse("2026-01-01T10:00:00Z");
        when(competitionUseCaseService.create(any())).thenReturn(
                new CompetitionResponse(id, "Cup", LocalDate.now(), true, false, List.of(), List.of(), now, now));

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
