package se.backede.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.backede.domain.service.GameRulesValidator;
import se.backede.domain.service.ScoreCalculator;
import se.backede.domain.service.WinnerCalculator;

@Configuration
public class DomainServiceConfig {

    @Bean
    GameRulesValidator gameRulesValidator() {
        return new GameRulesValidator();
    }

    @Bean
    WinnerCalculator winnerCalculator() {
        return new WinnerCalculator();
    }

    @Bean
    ScoreCalculator scoreCalculator() {
        return new ScoreCalculator();
    }
}
