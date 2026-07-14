package se.backede.infrastructure.web;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.AuthenticatedUser;
import se.backede.application.dto.UpdateCurrentUserRequest;
import se.backede.application.dto.UpdateUserRequest;
import se.backede.application.dto.UserResponse;
import se.backede.application.usecase.UserUseCaseService;
import se.backede.domain.model.UserRole;
import se.backede.infrastructure.security.AuthContext;
import se.backede.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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

@WebMvcTest({UserController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCaseService userUseCaseService;

    @AfterEach
    void clearAuthContext() {
        AuthContext.clear();
    }

    @Test
    void createsUser() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        when(userUseCaseService.create(new CreateUserRequest("admin", null, "secret12", UserRole.ADMIN, playerId)))
                .thenReturn(response(id, "admin", null, UserRole.ADMIN, playerId, "Alice"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest("admin", null, "secret12", UserRole.ADMIN, playerId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.playerName").value("Alice"));
    }

    @Test
    void createsUserWithNewPlayerCallsign() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var request = new CreateUserRequest("admin", null, "secret12", UserRole.ADMIN, null, "Ace");
        when(userUseCaseService.create(request))
                .thenReturn(response(id, "admin", null, UserRole.ADMIN, playerId, "Ace"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.playerName").value("Ace"));
    }

    @Test
    void returnsBadRequestForBlankUsername() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest("", null, "secret12", UserRole.USER, UUID.randomUUID()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void returnsBadRequestForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest("admin", "not-an-email", "secret12", UserRole.ADMIN, UUID.randomUUID()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listsUsers() throws Exception {
        when(userUseCaseService.list()).thenReturn(List.of(response(UUID.randomUUID(), "admin", null, UserRole.ADMIN, UUID.randomUUID(), "Alice")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    void getsUserById() throws Exception {
        var id = UUID.randomUUID();
        when(userUseCaseService.getById(id)).thenReturn(response(id, "admin", null, UserRole.ADMIN, UUID.randomUUID(), "Alice"));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void returnsNotFoundWhenUserDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        when(userUseCaseService.getById(id)).thenThrow(new ResourceNotFoundException("User not found: " + id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: " + id));
    }

    @Test
    void updatesUser() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        when(userUseCaseService.update(id, new UpdateUserRequest("user", null, null, UserRole.USER, playerId)))
                .thenReturn(response(id, "user", null, UserRole.USER, playerId, "Bob"));

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest("user", null, null, UserRole.USER, playerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatesCurrentUser() throws Exception {
        var id = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var request = new UpdateCurrentUserRequest("user@example.com", "Ace");
        AuthContext.set(new AuthenticatedUser(id, "user", UserRole.USER, playerId));
        when(userUseCaseService.updateMe(id, request))
                .thenReturn(response(id, "user", "user@example.com", UserRole.USER, playerId, "Ace"));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.playerName").value("Ace"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCurrentUserRejectsBlankPlayerCallsign() throws Exception {
        AuthContext.set(new AuthenticatedUser(UUID.randomUUID(), "user", UserRole.USER, UUID.randomUUID()));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCurrentUserRequest("user@example.com", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deletesUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found: " + id)).when(userUseCaseService).delete(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    private static UserResponse response(UUID id, String username, String email, UserRole role, UUID playerId, String playerName) {
        var now = Instant.parse("2026-01-01T10:00:00Z");
        return new UserResponse(id, username, email, role, playerId, playerName, now, now);
    }
}
