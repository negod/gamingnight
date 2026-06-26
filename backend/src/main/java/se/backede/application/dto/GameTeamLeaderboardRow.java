package se.backede.application.dto;

import java.util.UUID;

public record GameTeamLeaderboardRow(
        int rank,
        UUID teamId,
        String teamName,
        double value
) {
}
