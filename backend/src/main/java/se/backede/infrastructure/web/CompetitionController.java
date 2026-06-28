package se.backede.infrastructure.web;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.application.usecase.CompetitionUseCaseService;
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
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionUseCaseService competitionUseCaseService;

    public CompetitionController(CompetitionUseCaseService competitionUseCaseService) {
        this.competitionUseCaseService = competitionUseCaseService;
    }

    @PostMapping
    ResponseEntity<CompetitionResponse> create(@Valid @RequestBody CreateCompetitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(competitionUseCaseService.create(request));
    }

    @GetMapping
    List<CompetitionResponse> list() {
        var user = AuthContext.requireUser();
        return user.admin() ? competitionUseCaseService.list() : competitionUseCaseService.listForPlayer(user.playerId());
    }

    @GetMapping("/{id}")
    CompetitionResponse getById(@PathVariable UUID id) {
        var user = AuthContext.requireUser();
        return user.admin() ? competitionUseCaseService.getById(id) : competitionUseCaseService.getByIdForPlayer(id, user.playerId());
    }

    @PutMapping("/{id}")
    CompetitionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCompetitionRequest request) {
        return competitionUseCaseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        competitionUseCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-teams")
    CompetitionResponse generateTeams(@PathVariable UUID id, @Valid @RequestBody GenerateTeamsRequest request) {
        return competitionUseCaseService.generateTeams(id, request);
    }
}
