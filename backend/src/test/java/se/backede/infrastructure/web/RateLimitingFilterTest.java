package se.backede.infrastructure.web;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.EnterResultsRequest;
import se.backede.application.dto.LoginRequest;
import se.backede.application.dto.PlayerResultInput;
import se.backede.application.dto.SignupRequest;
import se.backede.application.usecase.AuthUseCaseService;
import se.backede.application.usecase.CompetitionRunUseCaseService;
import se.backede.application.usecase.CompetitionUseCaseService;
import se.backede.application.usecase.UserUseCaseService;
import se.backede.domain.model.UserRole;
import se.backede.infrastructure.config.RateLimitingFilter;
import se.backede.infrastructure.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UserController.class, CompetitionRunController.class, GlobalExceptionHandler.class})
@Import({SecurityConfig.class, RateLimitingFilter.class})
@WithMockUser(roles = "ADMIN")
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCaseService userUseCaseService;

    @MockBean
    private AuthUseCaseService authUseCaseService;

    @MockBean
    private CompetitionRunUseCaseService competitionRunUseCaseService;

    @MockBean
    private CompetitionUseCaseService competitionUseCaseService;

    @Test
    void returns429AfterTenLoginRequestsPerMinuteFromSameIp() throws Exception {
        RequestBuilder request = post("/api/auth/login")
                .with(httpRequest -> {
                    httpRequest.setRemoteAddr("10.10.0.4");
                    return httpRequest;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin")));

        expectTooManyRequestsAfterAllowedRequests(request, 10, 200);
    }

    @Test
    void returns429AfterFiveSignupRequestsPerMinuteFromSameIp() throws Exception {
        RequestBuilder request = post("/api/auth/signup")
                .with(httpRequest -> {
                    httpRequest.setRemoteAddr("10.10.0.5");
                    return httpRequest;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new SignupRequest("newuser", "newuser@example.com", "secret12")));

        expectTooManyRequestsAfterAllowedRequests(request, 5, 201);
    }

    @Test
    void returns429AfterFiveUserCreateRequestsPerMinuteFromSameIp() throws Exception {
        RequestBuilder request = post("/api/users")
                .with(httpRequest -> {
                    httpRequest.setRemoteAddr("10.10.0.1");
                    return httpRequest;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateUserRequest("admin", null, "secret12", UserRole.ADMIN, UUID.randomUUID())));

        expectTooManyRequestsAfterAllowedRequests(request, 5, 201);
    }

    @Test
    void returns429AfterTenCompetitionStartRequestsPerMinuteFromSameIp() throws Exception {
        RequestBuilder request = post("/api/competitions/{id}/start", UUID.randomUUID())
                .with(httpRequest -> {
                    httpRequest.setRemoteAddr("10.10.0.2");
                    return httpRequest;
                });

        expectTooManyRequestsAfterAllowedRequests(request, 10, 200);
    }

    @Test
    void returns429AfterThirtyResultEntryRequestsPerMinuteFromSameIp() throws Exception {
        var requestBody = new EnterResultsRequest(List.of(
                new PlayerResultInput(UUID.randomUUID(), UUID.randomUUID(), 100.0)
        ));
        RequestBuilder request = put("/api/competitions/{cid}/matches/{mid}/results",
                UUID.randomUUID(), UUID.randomUUID())
                .with(httpRequest -> {
                    httpRequest.setRemoteAddr("10.10.0.3");
                    return httpRequest;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        expectTooManyRequestsAfterAllowedRequests(request, 30, 200);
    }

    private void expectTooManyRequestsAfterAllowedRequests(RequestBuilder request, int allowedRequests, int expectedAllowedStatus) throws Exception {
        for (int i = 0; i < allowedRequests; i++) {
            mockMvc.perform(request)
                    .andExpect(status().is(expectedAllowedStatus));
        }

        mockMvc.perform(request)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, notNullValue()))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }

}
