package se.backede.application.dto;

import java.util.UUID;

public record GamePlayerLeaderboardRow(
        int rank,
        UUID playerId,
        String playerName,
        double value
) {
}
