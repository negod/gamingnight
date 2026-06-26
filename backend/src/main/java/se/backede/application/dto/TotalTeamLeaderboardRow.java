package se.backede.application.dto;

import java.util.UUID;

public record TotalTeamLeaderboardRow(
        int rank,
        UUID teamId,
        String teamName,
        int points
) {
}
