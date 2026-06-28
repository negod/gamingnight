package se.backede.infrastructure.web;

import se.backede.application.dto.LoginRequest;
import se.backede.application.dto.LoginResponse;
import se.backede.application.dto.SignupRequest;
import se.backede.application.usecase.AuthUseCaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCaseService authUseCaseService;

    public AuthController(AuthUseCaseService authUseCaseService) {
        this.authUseCaseService = authUseCaseService;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authUseCaseService.login(request);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    LoginResponse signup(@Valid @RequestBody SignupRequest request) {
        return authUseCaseService.signup(request);
    }
}
