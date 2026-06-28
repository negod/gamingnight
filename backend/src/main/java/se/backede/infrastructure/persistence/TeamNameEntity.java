package se.backede.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_names")
public class TeamNameEntity {

    @Id
    @Column(nullable = false, length = 120)
    private String name;

    protected TeamNameEntity() {
    }

    public TeamNameEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
