package se.backede.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
        include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WinDrawLossScoringRule.class, name = "WIN_DRAW_LOSS"),
        @JsonSubTypes.Type(value = PlacementScoringRule.class, name = "PLACEMENT"),
        @JsonSubTypes.Type(value = ScoreBasedScoringRule.class, name = "SCORE_BASED"),
        @JsonSubTypes.Type(value = WinnerTakesAllScoringRule.class, name = "WINNER_TAKES_ALL"),
        @JsonSubTypes.Type(value = ManualScoringRule.class, name = "MANUAL"),
})
public sealed interface ScoringRule
        permits WinDrawLossScoringRule, PlacementScoringRule, ScoreBasedScoringRule,
                WinnerTakesAllScoringRule, ManualScoringRule {
    ScoringRuleType type();
}
