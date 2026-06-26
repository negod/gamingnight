package se.backede.infrastructure.web;

import se.backede.application.dto.CreateTeamRequest;
import se.backede.application.dto.TeamResponse;
import se.backede.application.dto.UpdateTeamRequest;
import se.backede.application.usecase.TeamUseCaseService;
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
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamUseCaseService teamUseCaseService;

    public TeamController(TeamUseCaseService teamUseCaseService) {
        this.teamUseCaseService = teamUseCaseService;
    }

    @PostMapping
    ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamUseCaseService.create(request));
    }

    @GetMapping
    List<TeamResponse> list() {
        return teamUseCaseService.list();
    }

    @GetMapping("/{id}")
    TeamResponse getById(@PathVariable UUID id) {
        return teamUseCaseService.getById(id);
    }

    @PutMapping("/{id}")
    TeamResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return teamUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
