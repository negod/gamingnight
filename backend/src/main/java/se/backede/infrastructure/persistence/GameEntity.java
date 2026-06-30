package se.backede.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import se.backede.domain.model.BonusRule;
import se.backede.domain.model.MatchType;
import se.backede.domain.model.ParticipantRule;
import se.backede.domain.model.ResultType;
import se.backede.domain.model.RotationRule;
import se.backede.domain.model.ScoringRule;
import se.backede.domain.model.TieBreakerRule;
import se.backede.domain.model.TimeLimitRule;
import se.backede.domain.model.ValidationRule;
import se.backede.domain.model.WinnerRule;
import se.backede.infrastructure.persistence.converter.BonusRulesConverter;
import se.backede.infrastructure.persistence.converter.ParticipantRuleConverter;
import se.backede.infrastructure.persistence.converter.ScoringRuleConverter;
import se.backede.infrastructure.persistence.converter.TimeLimitRuleConverter;
import se.backede.infrastructure.persistence.converter.ValidationRuleConverter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column
    private String description;

    @Column(length = 120)
    private String platform;

    @Column(length = 120)
    private String genre;

    @Column(nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchType matchType;

    @Convert(converter = ParticipantRuleConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private ParticipantRule participantRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultType resultType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WinnerRule winnerRule;

    @Convert(converter = ScoringRuleConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private ScoringRule scoringRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TieBreakerRule tieBreakerRule;

    @Convert(converter = ValidationRuleConverter.class)
    @Column(columnDefinition = "text")
    private ValidationRule validationRule;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RotationRule rotationRule;

    @Convert(converter = TimeLimitRuleConverter.class)
    @Column(columnDefinition = "text")
    private TimeLimitRule timeLimitRule;

    @Convert(converter = BonusRulesConverter.class)
    @Column(columnDefinition = "text")
    private List<BonusRule> bonusRules;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected GameEntity() {
    }

    public GameEntity(UUID id, String name, String description, String platform, String genre,
                      boolean isActive, MatchType matchType, ParticipantRule participantRule,
                      ResultType resultType, WinnerRule winnerRule, ScoringRule scoringRule,
                      TieBreakerRule tieBreakerRule, ValidationRule validationRule,
                      RotationRule rotationRule, TimeLimitRule timeLimitRule,
                      List<BonusRule> bonusRules, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.platform = platform;
        this.genre = genre;
        this.isActive = isActive;
        this.matchType = matchType;
        this.participantRule = participantRule;
        this.resultType = resultType;
        this.winnerRule = winnerRule;
        this.scoringRule = scoringRule;
        this.tieBreakerRule = tieBreakerRule;
        this.validationRule = validationRule;
        this.rotationRule = rotationRule;
        this.timeLimitRule = timeLimitRule;
        this.bonusRules = bonusRules;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPlatform() { return platform; }
    public String getGenre() { return genre; }
    public boolean isActive() { return isActive; }
    public MatchType getMatchType() { return matchType; }
    public ParticipantRule getParticipantRule() { return participantRule; }
    public ResultType getResultType() { return resultType; }
    public WinnerRule getWinnerRule() { return winnerRule; }
    public ScoringRule getScoringRule() { return scoringRule; }
    public TieBreakerRule getTieBreakerRule() { return tieBreakerRule; }
    public ValidationRule getValidationRule() { return validationRule; }
    public RotationRule getRotationRule() { return rotationRule; }
    public TimeLimitRule getTimeLimitRule() { return timeLimitRule; }
    public List<BonusRule> getBonusRules() { return bonusRules; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setActive(boolean active) { this.isActive = active; }
    public void setMatchType(MatchType matchType) { this.matchType = matchType; }
    public void setParticipantRule(ParticipantRule participantRule) { this.participantRule = participantRule; }
    public void setResultType(ResultType resultType) { this.resultType = resultType; }
    public void setWinnerRule(WinnerRule winnerRule) { this.winnerRule = winnerRule; }
    public void setScoringRule(ScoringRule scoringRule) { this.scoringRule = scoringRule; }
    public void setTieBreakerRule(TieBreakerRule tieBreakerRule) { this.tieBreakerRule = tieBreakerRule; }
    public void setValidationRule(ValidationRule validationRule) { this.validationRule = validationRule; }
    public void setRotationRule(RotationRule rotationRule) { this.rotationRule = rotationRule; }
    public void setTimeLimitRule(TimeLimitRule timeLimitRule) { this.timeLimitRule = timeLimitRule; }
    public void setBonusRules(List<BonusRule> bonusRules) { this.bonusRules = bonusRules; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
