package se.backede.domain.service;

import se.backede.domain.model.Game;
import se.backede.domain.model.PlacementScoringRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.WinnerRule;
import se.backede.shared.exception.DomainValidationException;

public class GameRulesValidator {

    public void validate(Game game) {
        if (game.resultType() == ResultType.TIME && game.winnerRule() == WinnerRule.HIGHEST_VALUE_WINS) {
            throw new DomainValidationException(
                    "TIME result type must not use HIGHEST_VALUE_WINS — use LOWEST_VALUE_WINS instead");
        }

        if (game.resultType() == ResultType.PLACEMENT && !(game.scoringRule() instanceof PlacementScoringRule)) {
            throw new DomainValidationException(
                    "PLACEMENT result type requires PlacementScoringRule");
        }

        if (game.winnerRule() == WinnerRule.CLOSEST_TO_TARGET && game.validationRule() == null) {
            throw new DomainValidationException(
                    "CLOSEST_TO_TARGET winner rule requires a validationRule to define the target range");
        }
    }
}
