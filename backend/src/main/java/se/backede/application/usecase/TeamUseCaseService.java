package se.backede.application.usecase;

import se.backede.application.dto.CreateTeamRequest;
import se.backede.application.dto.TeamResponse;
import se.backede.application.dto.UpdateTeamRequest;
import se.backede.application.mapper.TeamDtoMapper;
import se.backede.domain.model.Team;
import se.backede.domain.repository.TeamRepositoryPort;
import se.backede.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TeamUseCaseService {

    private final TeamRepositoryPort repository;
    private final Clock clock;

    public TeamUseCaseService(TeamRepositoryPort repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public TeamResponse create(CreateTeamRequest request) {
        var team = Team.create(request.name(), request.playerIds(), now());
        return TeamDtoMapper.toResponse(repository.save(team));
    }

    public List<TeamResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Team::createdAt).reversed())
                .map(TeamDtoMapper::toResponse)
                .toList();
    }

    public TeamResponse getById(UUID id) {
        return repository.findById(id)
                .map(TeamDtoMapper::toResponse)
                .orElseThrow(() -> teamNotFound(id));
    }

    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        var team = repository.findById(id).orElseThrow(() -> teamNotFound(id));
        var updated = team.update(request.name(), request.playerIds(), now());
        return TeamDtoMapper.toResponse(repository.save(updated));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw teamNotFound(id);
        }
        repository.deleteById(id);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static ResourceNotFoundException teamNotFound(UUID id) {
        return new ResourceNotFoundException("Team not found: " + id);
    }
}
