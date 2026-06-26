package se.backede.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import se.backede.domain.model.CalculationMethod;
import se.backede.domain.model.GameType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalculationMethod calculationMethod;

    @Column
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected GameEntity() {
    }

    public GameEntity(UUID id, String name, GameType gameType, CalculationMethod calculationMethod,
                      String description, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.gameType = gameType;
        this.calculationMethod = calculationMethod;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public GameType getGameType() { return gameType; }
    public CalculationMethod getCalculationMethod() { return calculationMethod; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }
    public void setCalculationMethod(CalculationMethod calculationMethod) { this.calculationMethod = calculationMethod; }
    public void setDescription(String description) { this.description = description; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
