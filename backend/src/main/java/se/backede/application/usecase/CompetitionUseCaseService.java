package se.backede.application.usecase;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.application.mapper.CompetitionDtoMapper;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Team;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.repository.PlayerRepositoryPort;
import se.backede.domain.repository.TeamNameRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CompetitionUseCaseService {

    private final CompetitionRepositoryPort competitionRepository;
    private final GameRepositoryPort gameRepository;
    private final TeamRepositoryPort teamRepository;
    private final TeamNameRepositoryPort teamNameRepository;
    private final PlayerRepositoryPort playerRepository;
    private final Clock clock;

    public CompetitionUseCaseService(CompetitionRepositoryPort competitionRepository,
                                     GameRepositoryPort gameRepository,
                                     TeamRepositoryPort teamRepository,
                                     TeamNameRepositoryPort teamNameRepository,
                                     PlayerRepositoryPort playerRepository,
                                     Clock clock) {
        this.competitionRepository = competitionRepository;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.teamNameRepository = teamNameRepository;
        this.playerRepository = playerRepository;
        this.clock = clock;
    }

    public CompetitionResponse create(CreateCompetitionRequest request) {
        validateGamesExist(request.gameIds());
        validateTeamsExist(request.teamIds());
        var competition = Competition.create(request.name(), request.date(), request.singleMatch(), now());
        var withGamesAndTeams = competition.update(
                competition.name(), competition.date(), competition.singleMatch(),
                request.gameIds(), request.teamIds(), now()
        );
        return CompetitionDtoMapper.toResponse(competitionRepository.save(withGamesAndTeams));
    }

    public List<CompetitionResponse> list() {
        return competitionRepository.findAll().stream()
                .sorted(Comparator.comparing(Competition::date).reversed())
                .map(CompetitionDtoMapper::toResponse)
                .toList();
    }

    public List<CompetitionResponse> listForPlayer(UUID playerId) {
        return competitionRepository.findAll().stream()
                .filter(competition -> playerHasTeamInCompetition(playerId, competition))
                .sorted(Comparator.comparing(Competition::date).reversed())
                .map(CompetitionDtoMapper::toResponse)
                .toList();
    }

    public CompetitionResponse getById(UUID id) {
        return competitionRepository.findById(id)
                .map(CompetitionDtoMapper::toResponse)
                .orElseThrow(() -> competitionNotFound(id));
    }

    public CompetitionResponse getByIdForPlayer(UUID id, UUID playerId) {
        var competition = competitionRepository.findById(id)
                .orElseThrow(() -> competitionNotFound(id));
        if (!playerHasTeamInCompetition(playerId, competition)) {
            throw competitionNotFound(id);
        }
        return CompetitionDtoMapper.toResponse(competition);
    }

    public boolean playerCanAccessCompetition(UUID competitionId, UUID playerId) {
        return competitionRepository.findById(competitionId)
                .map(competition -> playerHasTeamInCompetition(playerId, competition))
                .orElse(false);
    }

    public CompetitionResponse update(UUID id, UpdateCompetitionRequest request) {
        var competition = competitionRepository.findById(id).orElseThrow(() -> competitionNotFound(id));
        if (competition.started()) {
            throw new DomainValidationException("Cannot edit a started competition");
        }
        validateGamesExist(request.gameIds());
        validateTeamsExist(request.teamIds());
        var updated = competition.update(
                request.name(), request.date(), request.singleMatch(),
                request.gameIds(), request.teamIds(), now()
        );
        return CompetitionDtoMapper.toResponse(competitionRepository.save(updated));
    }

    public void delete(UUID id) {
        if (!competitionRepository.existsById(id)) {
            throw competitionNotFound(id);
        }
        competitionRepository.deleteById(id);
    }

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

    private boolean playerHasTeamInCompetition(UUID playerId, Competition competition) {
        for (UUID teamId : competition.teamIds()) {
            var team = teamRepository.findById(teamId);
            if (team.isPresent() && team.get().playerIds().contains(playerId)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private void validateGamesExist(List<UUID> gameIds) {
        for (UUID gameId : gameIds) {
            if (!gameRepository.existsById(gameId)) {
                throw new ResourceNotFoundException("Game not found: " + gameId);
            }
        }
    }

    private void validateTeamsExist(List<UUID> teamIds) {
        for (UUID teamId : teamIds) {
            if (!teamRepository.existsById(teamId)) {
                throw new ResourceNotFoundException("Team not found: " + teamId);
            }
        }
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
