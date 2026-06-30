package se.backede.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.backede.domain.model.ParticipantRule;

@Converter
public class ParticipantRuleConverter implements AttributeConverter<ParticipantRule, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(ParticipantRule attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ParticipantRule", e);
        }
    }

    @Override
    public ParticipantRule convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, ParticipantRule.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize ParticipantRule: " + dbData, e);
        }
    }
}
