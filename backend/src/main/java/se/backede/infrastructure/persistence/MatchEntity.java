package se.backede.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID competitionId;

    @Column(nullable = false)
    private UUID gameId;

    @Column(nullable = false)
    private UUID homeTeamId;

    @Column(nullable = false)
    private UUID awayTeamId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    private List<PlayerResultEntity> results = new ArrayList<>();

    protected MatchEntity() {
    }

    public MatchEntity(UUID id, UUID competitionId, UUID gameId, UUID homeTeamId, UUID awayTeamId,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.competitionId = competitionId;
        this.gameId = gameId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getCompetitionId() { return competitionId; }
    public UUID getGameId() { return gameId; }
    public UUID getHomeTeamId() { return homeTeamId; }
    public UUID getAwayTeamId() { return awayTeamId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<PlayerResultEntity> getResults() { return results; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
