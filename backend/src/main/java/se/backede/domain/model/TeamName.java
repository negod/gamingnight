package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

public record TeamName(String name) {

    public TeamName {
        name = name == null ? "" : name.trim();
        if (name.isBlank()) throw new DomainValidationException("Team name is required");
        if (name.length() > Team.NAME_MAX_LENGTH) throw new DomainValidationException("Team name must be at most 120 characters");
    }
}
