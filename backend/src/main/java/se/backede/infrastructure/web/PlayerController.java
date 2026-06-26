package se.backede.infrastructure.web;

import se.backede.application.dto.CreatePlayerRequest;
import se.backede.application.dto.PlayerResponse;
import se.backede.application.dto.UpdatePlayerRequest;
import se.backede.application.usecase.PlayerUseCaseService;
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
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerUseCaseService playerUseCaseService;

    public PlayerController(PlayerUseCaseService playerUseCaseService) {
        this.playerUseCaseService = playerUseCaseService;
    }

    @PostMapping
    ResponseEntity<PlayerResponse> create(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerUseCaseService.create(request));
    }

    @GetMapping
    List<PlayerResponse> list() {
        return playerUseCaseService.list();
    }

    @GetMapping("/{id}")
    PlayerResponse getById(@PathVariable UUID id) {
        return playerUseCaseService.getById(id);
    }

    @PutMapping("/{id}")
    PlayerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePlayerRequest request) {
        return playerUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        playerUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
