package se.backede.infrastructure.web;

import se.backede.application.dto.GamePlayerLeaderboardResponse;
import se.backede.application.dto.GamePlayerLeaderboardRow;
import se.backede.application.dto.GameTeamLeaderboardResponse;
import se.backede.application.dto.GameTeamLeaderboardRow;
import se.backede.application.dto.TotalPlayerLeaderboardRow;
import se.backede.application.dto.TotalTeamLeaderboardRow;
import se.backede.application.usecase.LeaderboardUseCaseService;
import se.backede.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({LeaderboardController.class, GlobalExceptionHandler.class})
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardUseCaseService service;

    @Test
    void gameTeamLeaderboardReturnsRankedRows() throws Exception {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var row = new GameTeamLeaderboardRow(1, teamId, "Alpha", 500.0);
        when(service.getGameTeamLeaderboard(competitionId, gameId))
                .thenReturn(new GameTeamLeaderboardResponse("Total Score", List.of(row)));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/games/{gid}/teams", competitionId, gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnHeader").value("Total Score"))
                .andExpect(jsonPath("$.rows[0].teamName").value("Alpha"))
                .andExpect(jsonPath("$.rows[0].rank").value(1));
    }

    @Test
    void gameTeamLeaderboardReturns404WhenGameNotFound() throws Exception {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        when(service.getGameTeamLeaderboard(competitionId, gameId))
                .thenThrow(new ResourceNotFoundException("Game not found: " + gameId));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/games/{gid}/teams", competitionId, gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void gamePlayerLeaderboardReturnsRankedRows() throws Exception {
        var competitionId = UUID.randomUUID();
        var gameId = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var row = new GamePlayerLeaderboardRow(1, playerId, "Alice", 42.5);
        when(service.getGamePlayerLeaderboard(competitionId, gameId))
                .thenReturn(new GamePlayerLeaderboardResponse("Average Time", List.of(row)));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/games/{gid}/players", competitionId, gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnHeader").value("Average Time"))
                .andExpect(jsonPath("$.rows[0].playerName").value("Alice"))
                .andExpect(jsonPath("$.rows[0].value").value(42.5));
    }

    @Test
    void totalTeamLeaderboardReturnsPointRows() throws Exception {
        var competitionId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var row = new TotalTeamLeaderboardRow(1, teamId, "Winners", 100);
        when(service.getTotalTeamLeaderboard(competitionId)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/teams", competitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Winners"))
                .andExpect(jsonPath("$[0].points").value(100))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void totalTeamLeaderboardReturns404WhenCompetitionNotFound() throws Exception {
        var competitionId = UUID.randomUUID();
        when(service.getTotalTeamLeaderboard(competitionId))
                .thenThrow(new ResourceNotFoundException("Competition not found: " + competitionId));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/teams", competitionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void totalPlayerLeaderboardReturnsPointRows() throws Exception {
        var competitionId = UUID.randomUUID();
        var playerId = UUID.randomUUID();
        var row = new TotalPlayerLeaderboardRow(1, playerId, "Alice", 190);
        when(service.getTotalPlayerLeaderboard(competitionId)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/competitions/{cid}/leaderboard/players", competitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerName").value("Alice"))
                .andExpect(jsonPath("$[0].points").value(190));
    }
}
