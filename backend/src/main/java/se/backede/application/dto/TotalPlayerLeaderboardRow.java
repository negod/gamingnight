package se.backede.application.dto;

import java.util.UUID;

public record TotalPlayerLeaderboardRow(
        int rank,
        UUID playerId,
        String playerName,
        int points
) {
}
