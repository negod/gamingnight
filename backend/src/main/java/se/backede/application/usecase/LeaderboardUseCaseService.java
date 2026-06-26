package se.backede.application.usecase;

import se.backede.application.dto.GamePlayerLeaderboardResponse;
import se.backede.application.dto.GamePlayerLeaderboardRow;
import se.backede.application.dto.GameTeamLeaderboardResponse;
import se.backede.application.dto.GameTeamLeaderboardRow;
import se.backede.application.dto.TotalPlayerLeaderboardRow;
import se.backede.application.dto.TotalTeamLeaderboardRow;
import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.Game;
import se.backede.domain.model.GameType;
import se.backede.domain.model.Match;
import se.backede.domain.model.PlayerResult;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.repository.MatchRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LeaderboardUseCaseService {

    private final CompetitionRepositoryPort competitionRepository;
    private final MatchRepositoryPort matchRepository;
    private final GameRepositoryPort gameRepository;
    private final TeamRepositoryPort teamRepository;
    private final PlayerRepositoryPort playerRepository;

    public LeaderboardUseCaseService(CompetitionRepositoryPort competitionRepository,
                                     MatchRepositoryPort matchRepository,
                                     GameRepositoryPort gameRepository,
                                     TeamRepositoryPort teamRepository,
                                     PlayerRepositoryPort playerRepository) {
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    public GameTeamLeaderboardResponse getGameTeamLeaderboard(UUID competitionId, UUID gameId) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
        var rows = computeTeamRows(competitionId, gameId, game);
        return new GameTeamLeaderboardResponse(columnHeader(game), rows);
    }

    public GamePlayerLeaderboardResponse getGamePlayerLeaderboard(UUID competitionId, UUID gameId) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
        var rows = computePlayerRows(competitionId, gameId, game);
        return new GamePlayerLeaderboardResponse(columnHeader(game), rows);
    }

    public List<TotalTeamLeaderboardRow> getTotalTeamLeaderboard(UUID competitionId) {
        var competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + competitionId));

        Map<UUID, Integer> totalPoints = new HashMap<>();
        for (UUID teamId : competition.teamIds()) {
            totalPoints.put(teamId, 0);
        }

        for (UUID gameId : competition.gameIds()) {
            var game = gameRepository.findById(gameId).orElse(null);
            if (game == null) continue;
            var perGame = computeTeamRows(competitionId, gameId, game);
            for (var row : perGame) {
                totalPoints.merge(row.teamId(), placementPoints(row.rank()), Integer::sum);
            }
        }

        var sorted = totalPoints.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();

        var rows = new ArrayList<TotalTeamLeaderboardRow>();
        for (int i = 0; i < sorted.size(); i++) {
            var entry = sorted.get(i);
            int rank = i > 0 && sorted.get(i - 1).getValue().equals(entry.getValue())
                    ? rows.get(i - 1).rank() : i + 1;
            String name = teamRepository.findById(entry.getKey()).map(t -> t.name()).orElse("Unknown");
            rows.add(new TotalTeamLeaderboardRow(rank, entry.getKey(), name, entry.getValue()));
        }
        return rows;
    }

    public List<TotalPlayerLeaderboardRow> getTotalPlayerLeaderboard(UUID competitionId) {
        var competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + competitionId));

        Map<UUID, Integer> totalPoints = new HashMap<>();

        for (UUID gameId : competition.gameIds()) {
            var game = gameRepository.findById(gameId).orElse(null);
            if (game == null) continue;
            var perGame = computePlayerRows(competitionId, gameId, game);
            for (var row : perGame) {
                totalPoints.merge(row.playerId(), placementPoints(row.rank()), Integer::sum);
            }
        }

        var sorted = totalPoints.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();

        var rows = new ArrayList<TotalPlayerLeaderboardRow>();
        for (int i = 0; i < sorted.size(); i++) {
            var entry = sorted.get(i);
            int rank = i > 0 && sorted.get(i - 1).getValue().equals(entry.getValue())
                    ? rows.get(i - 1).rank() : i + 1;
            String name = playerRepository.findById(entry.getKey()).map(p -> p.name()).orElse("Unknown");
            rows.add(new TotalPlayerLeaderboardRow(rank, entry.getKey(), name, entry.getValue()));
        }
        return rows;
    }

    private List<GameTeamLeaderboardRow> computeTeamRows(UUID competitionId, UUID gameId, Game game) {
        var matches = matchRepository.findByCompetitionIdAndGameId(competitionId, gameId);

        Map<UUID, List<Double>> teamValues = new HashMap<>();
        for (Match match : matches) {
            for (PlayerResult result : match.results()) {
                teamValues.computeIfAbsent(result.teamId(), k -> new ArrayList<>()).add(result.value());
            }
        }

        Map<UUID, Double> aggregates = new HashMap<>();
        for (var entry : teamValues.entrySet()) {
            aggregates.put(entry.getKey(), aggregate(entry.getValue(), game.calculationMethod()));
        }

        Comparator<Map.Entry<UUID, Double>> cmp = Map.Entry.comparingByValue();
        if (game.gameType() == GameType.SCORE_BASED) cmp = cmp.reversed();
        var sorted = aggregates.entrySet().stream().sorted(cmp).toList();

        var rows = new ArrayList<GameTeamLeaderboardRow>();
        for (int i = 0; i < sorted.size(); i++) {
            var entry = sorted.get(i);
            int rank = i > 0 && Double.compare(sorted.get(i - 1).getValue(), entry.getValue()) == 0
                    ? rows.get(i - 1).rank() : i + 1;
            String name = teamRepository.findById(entry.getKey()).map(t -> t.name()).orElse("Unknown");
            rows.add(new GameTeamLeaderboardRow(rank, entry.getKey(), name, entry.getValue()));
        }
        return rows;
    }

    private List<GamePlayerLeaderboardRow> computePlayerRows(UUID competitionId, UUID gameId, Game game) {
        var matches = matchRepository.findByCompetitionIdAndGameId(competitionId, gameId);

        Map<UUID, List<Double>> playerValues = new HashMap<>();
        for (Match match : matches) {
            for (PlayerResult result : match.results()) {
                playerValues.computeIfAbsent(result.playerId(), k -> new ArrayList<>()).add(result.value());
            }
        }

        Map<UUID, Double> aggregates = new HashMap<>();
        for (var entry : playerValues.entrySet()) {
            aggregates.put(entry.getKey(), aggregate(entry.getValue(), game.calculationMethod()));
        }

        Comparator<Map.Entry<UUID, Double>> cmp = Map.Entry.comparingByValue();
        if (game.gameType() == GameType.SCORE_BASED) cmp = cmp.reversed();
        var sorted = aggregates.entrySet().stream().sorted(cmp).toList();

        var rows = new ArrayList<GamePlayerLeaderboardRow>();
        for (int i = 0; i < sorted.size(); i++) {
            var entry = sorted.get(i);
            int rank = i > 0 && Double.compare(sorted.get(i - 1).getValue(), entry.getValue()) == 0
                    ? rows.get(i - 1).rank() : i + 1;
            String name = playerRepository.findById(entry.getKey()).map(p -> p.name()).orElse("Unknown");
            rows.add(new GamePlayerLeaderboardRow(rank, entry.getKey(), name, entry.getValue()));
        }
        return rows;
    }

    private static double aggregate(List<Double> values, CalculationMethod method) {
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return method == CalculationMethod.SUM ? sum : sum / values.size();
    }

    private static String columnHeader(Game game) {
        boolean isScore = game.gameType() == GameType.SCORE_BASED;
        boolean isSum = game.calculationMethod() == CalculationMethod.SUM;
        if (isScore) return isSum ? "Total Score" : "Average Score";
        return isSum ? "Total Time" : "Average Time";
    }

    private static int placementPoints(int rank) {
        return switch (rank) {
            case 1 -> 100;
            case 2 -> 90;
            case 3 -> 80;
            case 4 -> 70;
            case 5 -> 60;
            case 6 -> 50;
            case 7 -> 40;
            case 8 -> 30;
            case 9 -> 20;
            case 10 -> 10;
            default -> 0;
        };
    }
}
