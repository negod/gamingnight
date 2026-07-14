package se.backede.application.usecase;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.application.mapper.CompetitionDtoMapper;
import se.backede.domain.model.Team;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamNameRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GenerateTeamsUseCaseService {

    private final CompetitionRepositoryPort competitionRepository;
    private final TeamRepositoryPort teamRepository;
    private final PlayerRepositoryPort playerRepository;
    private final TeamNameRepositoryPort teamNameRepository;
    private final Clock clock;

    public GenerateTeamsUseCaseService(CompetitionRepositoryPort competitionRepository,
                                       TeamRepositoryPort teamRepository,
                                       PlayerRepositoryPort playerRepository,
                                       TeamNameRepositoryPort teamNameRepository,
                                       Clock clock) {
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamNameRepository = teamNameRepository;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CompetitionResponse generateTeams(UUID competitionId, GenerateTeamsRequest request) {
        var competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> competitionNotFound(competitionId));
        if (competition.started()) {
            throw new DomainValidationException("Cannot modify a started competition");
        }
        validatePlayersExist(request.playerIds());

        List<UUID> shuffledPlayers = new ArrayList<>(request.playerIds());
        Collections.shuffle(shuffledPlayers);

        int numTeams = Math.max(1, shuffledPlayers.size() / request.teamSize());
        List<List<UUID>> buckets = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) {
            int from = i * request.teamSize();
            int to = Math.min(from + request.teamSize(), shuffledPlayers.size());
            buckets.add(new ArrayList<>(shuffledPlayers.subList(from, to)));
        }
        // distribute leftover players one per team, cycling from the first team
        int leftoverStart = numTeams * request.teamSize();
        for (int i = leftoverStart; i < shuffledPlayers.size(); i++) {
            buckets.get(i - leftoverStart).add(shuffledPlayers.get(i));
        }

        List<String> namePool = new ArrayList<>(unusedTeamNames());
        if (namePool.size() < buckets.size()) {
            throw new DomainValidationException("Not enough unused team names available");
        }
        Collections.shuffle(namePool);

        Instant now = now();
        List<UUID> newTeamIds = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            var team = Team.create(namePool.get(i), buckets.get(i), now);
            newTeamIds.add(teamRepository.save(team).id());
        }

        var updated = competition.withTeams(newTeamIds, now);
        return CompetitionDtoMapper.toResponse(competitionRepository.save(updated));
    }

    private List<String> unusedTeamNames() {
        var usedNames = new HashSet<String>();
        for (Team team : teamRepository.findAll()) {
            usedNames.add(normalizeName(team.name()));
        }
        return teamNameRepository.findAll().stream()
                .map(teamName -> teamName.name())
                .filter(name -> !usedNames.contains(normalizeName(name)))
                .toList();
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private void validatePlayersExist(List<UUID> playerIds) {
        for (UUID playerId : playerIds) {
            if (!playerRepository.existsById(playerId)) {
                throw new ResourceNotFoundException("Player not found: " + playerId);
            }
        }
    }

    private static ResourceNotFoundException competitionNotFound(UUID id) {
        return new ResourceNotFoundException("Competition not found: " + id);
    }
}
