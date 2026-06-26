package se.backede.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_player_ids", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "player_id", nullable = false)
    private Set<UUID> playerIds = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TeamEntity() {
    }

    public TeamEntity(UUID id, String name, Set<UUID> playerIds, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.playerIds = playerIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Set<UUID> getPlayerIds() { return playerIds; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
