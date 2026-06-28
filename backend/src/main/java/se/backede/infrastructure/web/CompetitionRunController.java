package se.backede.infrastructure.web;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.EnterResultsRequest;
import se.backede.application.dto.MatchResponse;
import se.backede.application.usecase.CompetitionRunUseCaseService;
import se.backede.application.usecase.CompetitionUseCaseService;
import se.backede.infrastructure.security.AuthContext;
import se.backede.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
public class CompetitionRunController {

    private final CompetitionRunUseCaseService service;
    private final CompetitionUseCaseService competitionUseCaseService;

    public CompetitionRunController(CompetitionRunUseCaseService service,
                                    CompetitionUseCaseService competitionUseCaseService) {
        this.service = service;
        this.competitionUseCaseService = competitionUseCaseService;
    }

    @PostMapping("/{id}/start")
    ResponseEntity<CompetitionResponse> start(@PathVariable UUID id) {
        return ResponseEntity.ok(service.start(id));
    }

    @GetMapping("/{competitionId}/games/{gameId}/matches")
    List<MatchResponse> getMatches(@PathVariable UUID competitionId, @PathVariable UUID gameId) {
        authorizeCompetitionAccess(competitionId);
        return service.getMatches(competitionId, gameId);
    }

    @PutMapping("/{competitionId}/matches/{matchId}/results")
    MatchResponse enterResults(
            @PathVariable UUID competitionId,
            @PathVariable UUID matchId,
            @Valid @RequestBody EnterResultsRequest request) {
        return service.enterResults(competitionId, matchId, request);
    }

    private void authorizeCompetitionAccess(UUID competitionId) {
        var user = AuthContext.requireUser();
        if (!user.admin() && !competitionUseCaseService.playerCanAccessCompetition(competitionId, user.playerId())) {
            throw new ResourceNotFoundException("Competition not found: " + competitionId);
        }
    }
}
