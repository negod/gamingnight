package se.backede.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataGameRepository extends JpaRepository<GameEntity, UUID> {
}
