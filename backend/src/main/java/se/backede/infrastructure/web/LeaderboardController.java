package se.backede.infrastructure.web;

import se.backede.application.dto.GamePlayerLeaderboardResponse;
import se.backede.application.dto.GameTeamLeaderboardResponse;
import se.backede.application.dto.TotalPlayerLeaderboardRow;
import se.backede.application.dto.TotalTeamLeaderboardRow;
import se.backede.application.usecase.LeaderboardUseCaseService;
import se.backede.application.usecase.CompetitionUseCaseService;
import se.backede.infrastructure.security.AuthContext;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions/{competitionId}/leaderboard")
public class LeaderboardController {

    private final LeaderboardUseCaseService service;
    private final CompetitionUseCaseService competitionUseCaseService;

    public LeaderboardController(LeaderboardUseCaseService service,
                                 CompetitionUseCaseService competitionUseCaseService) {
        this.service = service;
        this.competitionUseCaseService = competitionUseCaseService;
    }

    @GetMapping("/games/{gameId}/teams")
    GameTeamLeaderboardResponse gameTeamLeaderboard(
            @PathVariable UUID competitionId,
            @PathVariable UUID gameId) {
        authorizeCompetitionAccess(competitionId);
        return service.getGameTeamLeaderboard(competitionId, gameId);
    }

    @GetMapping("/games/{gameId}/players")
    GamePlayerLeaderboardResponse gamePlayerLeaderboard(
            @PathVariable UUID competitionId,
            @PathVariable UUID gameId) {
        authorizeCompetitionAccess(competitionId);
        return service.getGamePlayerLeaderboard(competitionId, gameId);
    }

    @GetMapping("/teams")
    List<TotalTeamLeaderboardRow> totalTeamLeaderboard(@PathVariable UUID competitionId) {
        authorizeCompetitionAccess(competitionId);
        return service.getTotalTeamLeaderboard(competitionId);
    }

    @GetMapping("/players")
    List<TotalPlayerLeaderboardRow> totalPlayerLeaderboard(@PathVariable UUID competitionId) {
        authorizeCompetitionAccess(competitionId);
        return service.getTotalPlayerLeaderboard(competitionId);
    }

    private void authorizeCompetitionAccess(UUID competitionId) {
        var user = AuthContext.requireUser();
        if (!user.admin() && !competitionUseCaseService.playerCanAccessCompetition(competitionId, user.playerId())) {
            throw new ResourceNotFoundException("Competition not found: " + competitionId);
        }
    }
}
