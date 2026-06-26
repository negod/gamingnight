package se.backede.application.usecase;

import se.backede.application.dto.CompetitionResponse;
import se.backede.application.dto.CreateCompetitionRequest;
import se.backede.application.dto.GenerateTeamsRequest;
import se.backede.application.dto.UpdateCompetitionRequest;
import se.backede.application.mapper.CompetitionDtoMapper;
import se.backede.domain.model.Competition;
import se.backede.domain.model.Team;
import se.backede.domain.repository.CompetitionRepositoryPort;
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
import java.util.List;
import java.util.UUID;

@Service
public class CompetitionUseCaseService {

    private static final List<String> TEAM_NAMES = List.of(
            "Thunder Hawks", "Iron Wolves", "Crimson Bears", "Storm Eagles",
            "Shadow Lions", "Frost Giants", "Blaze Foxes", "Dark Knights",
            "Solar Falcons", "Night Owls", "Wild Cards", "Steel Cobras",
            "Golden Rams", "Rapid Vipers", "Silent Jaguars", "Power Surge",
            "Cosmic Riders", "Neon Tigers", "Phantom Wolves", "Ice Dragons"
    );

    private final CompetitionRepositoryPort competitionRepository;
    private final TeamRepositoryPort teamRepository;
    private final Clock clock;

    public CompetitionUseCaseService(CompetitionRepositoryPort competitionRepository,
                                     TeamRepositoryPort teamRepository,
                                     Clock clock) {
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.clock = clock;
    }

    public CompetitionResponse create(CreateCompetitionRequest request) {
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

    public CompetitionResponse getById(UUID id) {
        return competitionRepository.findById(id)
                .map(CompetitionDtoMapper::toResponse)
                .orElseThrow(() -> competitionNotFound(id));
    }

    public CompetitionResponse update(UUID id, UpdateCompetitionRequest request) {
        var competition = competitionRepository.findById(id).orElseThrow(() -> competitionNotFound(id));
        if (competition.started()) {
            throw new DomainValidationException("Cannot edit a started competition");
        }
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

        List<String> namePool = new ArrayList<>(TEAM_NAMES);
        Collections.shuffle(namePool);

        Instant now = now();
        List<UUID> newTeamIds = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            String baseName = namePool.get(i % namePool.size());
            String teamName = i < namePool.size() ? baseName : baseName + " " + (i / namePool.size() + 1);
            var team = Team.create(teamName, buckets.get(i), now);
            newTeamIds.add(teamRepository.save(team).id());
        }

        var updated = competition.withTeams(newTeamIds, now);
        return CompetitionDtoMapper.toResponse(competitionRepository.save(updated));
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private static ResourceNotFoundException competitionNotFound(UUID id) {
        return new ResourceNotFoundException("Competition not found: " + id);
    }
}
