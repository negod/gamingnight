package se.backede.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.backede.domain.model.ScoringRule;

@Converter
public class ScoringRuleConverter implements AttributeConverter<ScoringRule, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(ScoringRule attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ScoringRule", e);
        }
    }

    @Override
    public ScoringRule convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, ScoringRule.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize ScoringRule: " + dbData, e);
        }
    }
}
