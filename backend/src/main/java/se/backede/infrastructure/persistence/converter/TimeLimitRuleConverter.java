package se.backede.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.backede.domain.model.TimeLimitRule;

@Converter
public class TimeLimitRuleConverter implements AttributeConverter<TimeLimitRule, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(TimeLimitRule attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize TimeLimitRule", e);
        }
    }

    @Override
    public TimeLimitRule convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, TimeLimitRule.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize TimeLimitRule: " + dbData, e);
        }
    }
}
