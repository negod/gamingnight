package se.backede.application.dto;

import java.util.List;

public record GamePlayerLeaderboardResponse(
        String columnHeader,
        List<GamePlayerLeaderboardRow> rows
) {
}
