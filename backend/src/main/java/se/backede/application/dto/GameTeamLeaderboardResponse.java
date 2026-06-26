package se.backede.application.dto;

import java.util.List;

public record GameTeamLeaderboardResponse(
        String columnHeader,
        List<GameTeamLeaderboardRow> rows
) {
}
