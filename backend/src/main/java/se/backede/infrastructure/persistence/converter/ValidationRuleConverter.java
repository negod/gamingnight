package se.backede.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.backede.domain.model.ValidationRule;

@Converter
public class ValidationRuleConverter implements AttributeConverter<ValidationRule, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(ValidationRule attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ValidationRule", e);
        }
    }

    @Override
    public ValidationRule convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, ValidationRule.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize ValidationRule: " + dbData, e);
        }
    }
}
