package se.backede.infrastructure.web;

import se.backede.application.dto.CreateUserRequest;
import se.backede.application.dto.UpdateUserRequest;
import se.backede.application.dto.UserResponse;
import se.backede.application.usecase.UserUseCaseService;
import se.backede.infrastructure.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCaseService userUseCaseService;

    public UserController(UserUseCaseService userUseCaseService) {
        this.userUseCaseService = userUseCaseService;
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userUseCaseService.create(request));
    }

    @GetMapping
    List<UserResponse> list() {
        return userUseCaseService.list();
    }

    @GetMapping("/me")
    UserResponse me() {
        return userUseCaseService.getMe(AuthContext.requireUser().id());
    }

    @GetMapping("/{id}")
    UserResponse getById(@PathVariable UUID id) {
        return userUseCaseService.getById(id);
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        userUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
