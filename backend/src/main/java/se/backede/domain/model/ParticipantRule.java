package se.backede.domain.model;

import se.backede.shared.exception.DomainValidationException;

public record ParticipantRule(
        int minPlayersPerTeam,
        int maxPlayersPerTeam,
        Integer numberOfTeams,
        boolean allowSubstitutes
) {
    public ParticipantRule {
        if (minPlayersPerTeam < 1) throw new DomainValidationException("minPlayersPerTeam must be >= 1");
        if (maxPlayersPerTeam < minPlayersPerTeam)
            throw new DomainValidationException("maxPlayersPerTeam must be >= minPlayersPerTeam");
        if (numberOfTeams != null && numberOfTeams < 2)
            throw new DomainValidationException("numberOfTeams must be >= 2, or null for any");
    }
}
