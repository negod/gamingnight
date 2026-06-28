package se.backede.domain.repository;

import se.backede.domain.model.TeamName;

import java.util.List;

public interface TeamNameRepositoryPort {

    List<TeamName> findAll();
}
