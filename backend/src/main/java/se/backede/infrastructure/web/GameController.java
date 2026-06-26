package se.backede.infrastructure.web;

import se.backede.application.dto.CreateGameRequest;
import se.backede.application.dto.GameResponse;
import se.backede.application.dto.UpdateGameRequest;
import se.backede.application.usecase.GameUseCaseService;
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
@RequestMapping("/api/games")
public class GameController {

    private final GameUseCaseService gameUseCaseService;

    public GameController(GameUseCaseService gameUseCaseService) {
        this.gameUseCaseService = gameUseCaseService;
    }

    @PostMapping
    ResponseEntity<GameResponse> create(@Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameUseCaseService.create(request));
    }

    @GetMapping
    List<GameResponse> list() {
        return gameUseCaseService.list();
    }

    @GetMapping("/{id}")
    GameResponse getById(@PathVariable UUID id) {
        return gameUseCaseService.getById(id);
    }

    @PutMapping("/{id}")
    GameResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateGameRequest request) {
        return gameUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        gameUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
