package se.backede.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "player_results")
public class PlayerResultEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private UUID teamId;

    @Column(nullable = false)
    private double value;

    protected PlayerResultEntity() {
    }

    public PlayerResultEntity(UUID id, UUID playerId, UUID teamId, double value) {
        this.id = id;
        this.playerId = playerId;
        this.teamId = teamId;
        this.value = value;
    }

    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    public UUID getTeamId() { return teamId; }
    public double getValue() { return value; }
}
