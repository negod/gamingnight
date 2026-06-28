package se.backede.application.usecase;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.EnterResultsRequest;
import se.backede.application.dto.MatchResponse;
import se.backede.application.mapper.CompetitionDtoMapper;
import se.backede.application.mapper.MatchDtoMapper;
import se.backede.domain.model.Match;
import se.backede.domain.model.PlayerResult;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.MatchRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompetitionRunUseCaseService {

    private final CompetitionRepositoryPort competitionRepository;
    private final MatchRepositoryPort matchRepository;
    private final TeamRepositoryPort teamRepository;
    private final PlayerRepositoryPort playerRepository;
    private final Clock clock;

    public CompetitionRunUseCaseService(CompetitionRepositoryPort competitionRepository,
                                        MatchRepositoryPort matchRepository,
                                        TeamRepositoryPort teamRepository,
                                        PlayerRepositoryPort playerRepository,
                                        Clock clock) {
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.clock = clock;
    }

    public CompetitionResponse start(UUID competitionId) {
        var competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + competitionId));

        if (competition.started()) {
            throw new DomainValidationException("Competition has already been started");
        }
        if (competition.teamIds().size() < 2) {
            throw new DomainValidationException("Competition must have at least 2 teams to start");
        }
        if (competition.gameIds().isEmpty()) {
            throw new DomainValidationException("Competition must have at least 1 game to start");
        }

        var now = now();
        var teams = competition.teamIds();

        List<Match> matches = new ArrayList<>();
        for (UUID gameId : competition.gameIds()) {
            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    matches.add(Match.create(competitionId, gameId, teams.get(i), teams.get(j), now));
                    if (!competition.singleMatch()) {
                        matches.add(Match.create(competitionId, gameId, teams.get(j), teams.get(i), now));
                    }
                }
            }
        }
        matches.forEach(matchRepository::save);

        var started = competition.start(now);
        return CompetitionDtoMapper.toResponse(competitionRepository.save(started));
    }

    public List<MatchResponse> getMatches(UUID competitionId, UUID gameId) {
        var matches = matchRepository.findByCompetitionIdAndGameId(competitionId, gameId);
        return matches.stream()
                .map(m -> {
                    String homeName = teamRepository.findById(m.homeTeamId())
                            .map(t -> t.name()).orElse("Unknown");
                    String awayName = teamRepository.findById(m.awayTeamId())
                            .map(t -> t.name()).orElse("Unknown");
                    Map<UUID, String> playerNames = resolvePlayerNames(m.results().stream()
                            .map(PlayerResult::playerId).toList());
                    return MatchDtoMapper.toResponse(m, homeName, awayName, playerNames);
                })
                .toList();
    }

    public MatchResponse enterResults(UUID competitionId, UUID matchId, EnterResultsRequest request) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        if (!match.competitionId().equals(competitionId)) {
            throw new ResourceNotFoundException("Match not found: " + matchId);
        }

        var results = request.results().stream()
                .map(r -> new PlayerResult(r.playerId(), r.teamId(), r.value()))
                .toList();

        var updated = match.withResults(results, now());
        var saved = matchRepository.save(updated);

        String homeName = teamRepository.findById(saved.homeTeamId()).map(t -> t.name()).orElse("Unknown");
        String awayName = teamRepository.findById(saved.awayTeamId()).map(t -> t.name()).orElse("Unknown");
        Map<UUID, String> playerNames = resolvePlayerNames(results.stream().map(PlayerResult::playerId).toList());
        return MatchDtoMapper.toResponse(saved, homeName, awayName, playerNames);
    }

    private Map<UUID, String> resolvePlayerNames(List<UUID> playerIds) {
        return playerIds.stream()
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> playerRepository.findById(id).map(p -> p.name()).orElse(id.toString())
                ));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
