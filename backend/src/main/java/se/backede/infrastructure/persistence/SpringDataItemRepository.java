package se.backede.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataItemRepository extends JpaRepository<ItemEntity, UUID> {
}
