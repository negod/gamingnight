package se.backede.application.usecase;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.application.mapper.CompetitionDtoMapper;
import se.backede.domain.model.Competition;
import se.backede.domain.repository.CompetitionRepositoryPort;
import se.backede.domain.repository.GameRepositoryPort;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.DomainValidationException;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CompetitionUseCaseService {

    private final CompetitionRepositoryPort competitionRepository;
    private final GameRepositoryPort gameRepository;
    private final TeamRepositoryPort teamRepository;
    private final Clock clock;

    public CompetitionUseCaseService(CompetitionRepositoryPort competitionRepository,
                                     GameRepositoryPort gameRepository,
                                     TeamRepositoryPort teamRepository,
                                     Clock clock) {
        this.competitionRepository = competitionRepository;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionResponse create(CreateCompetitionRequest request) {
        validateGamesExist(request.gameIds());
        validateTeamsExist(request.teamIds());
        var competition = Competition.create(request.name(), request.date(), request.singleMatch(), now());
        var withGamesAndTeams = competition.update(
                competition.name(), competition.date(), competition.singleMatch(), request.registrationOpen(),
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
                .filter(competition -> playerCanAccessCompetition(playerId, competition))
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
        if (!playerCanAccessCompetition(playerId, competition)) {
            throw competitionNotFound(id);
        }
        return CompetitionDtoMapper.toResponse(competition);
    }

    public boolean playerCanAccessCompetition(UUID competitionId, UUID playerId) {
        return competitionRepository.findById(competitionId)
                .map(competition -> playerCanAccessCompetition(playerId, competition))
                .orElse(false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionResponse update(UUID id, UpdateCompetitionRequest request) {
        var competition = competitionRepository.findById(id).orElseThrow(() -> competitionNotFound(id));
        if (competition.started()) {
            throw new DomainValidationException("Cannot edit a started competition");
        }
        validateGamesExist(request.gameIds());
        validateTeamsExist(request.teamIds());
        var updated = competition.update(
                request.name(), request.date(), request.singleMatch(), request.registrationOpen(),
                request.gameIds(), request.teamIds(), now()
        );
        return CompetitionDtoMapper.toResponse(competitionRepository.save(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID id) {
        if (!competitionRepository.existsById(id)) {
            throw competitionNotFound(id);
        }
        competitionRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public CompetitionResponse registerPlayer(UUID id, UUID playerId) {
        var competition = competitionRepository.findById(id).orElseThrow(() -> competitionNotFound(id));
        return CompetitionDtoMapper.toResponse(competitionRepository.save(competition.registerPlayer(playerId, now())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public CompetitionResponse unregisterPlayer(UUID id, UUID playerId) {
        var competition = competitionRepository.findById(id).orElseThrow(() -> competitionNotFound(id));
        return CompetitionDtoMapper.toResponse(competitionRepository.save(competition.unregisterPlayer(playerId, now())));
    }

    private boolean playerCanAccessCompetition(UUID playerId, Competition competition) {
        return (!competition.started() && competition.registrationOpen())
                || competition.registeredPlayerIds().contains(playerId)
                || playerHasTeamInCompetition(playerId, competition);
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

    private static ResourceNotFoundException competitionNotFound(UUID id) {
        return new ResourceNotFoundException("Competition not found: " + id);
    }
}
