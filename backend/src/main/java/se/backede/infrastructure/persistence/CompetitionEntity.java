package se.backede.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "competitions")
public class CompetitionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean singleMatch;

    @Column(nullable = false)
    private boolean started;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competition_game_ids", joinColumns = @JoinColumn(name = "competition_id"))
    @Column(name = "game_id", nullable = false)
    @OrderColumn(name = "position")
    private List<UUID> gameIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competition_team_ids", joinColumns = @JoinColumn(name = "competition_id"))
    @Column(name = "team_id", nullable = false)
    private Set<UUID> teamIds = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CompetitionEntity() {
    }

    public CompetitionEntity(UUID id, String name, LocalDate date, boolean singleMatch, boolean started,
                             List<UUID> gameIds, Set<UUID> teamIds, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.singleMatch = singleMatch;
        this.started = started;
        this.gameIds = gameIds;
        this.teamIds = teamIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public boolean isSingleMatch() { return singleMatch; }
    public boolean isStarted() { return started; }
    public List<UUID> getGameIds() { return gameIds; }
    public Set<UUID> getTeamIds() { return teamIds; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
