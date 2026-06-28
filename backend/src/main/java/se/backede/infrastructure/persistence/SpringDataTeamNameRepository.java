package se.backede.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTeamNameRepository extends JpaRepository<TeamNameEntity, String> {
}
