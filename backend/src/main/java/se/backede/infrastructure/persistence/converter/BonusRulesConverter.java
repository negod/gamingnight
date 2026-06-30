package se.backede.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.backede.domain.model.BonusRule;

import java.util.List;

@Converter
public class BonusRulesConverter implements AttributeConverter<List<BonusRule>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final TypeReference<List<BonusRule>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<BonusRule> attribute) {
        if (attribute == null || attribute.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize bonus rules", e);
        }
    }

    @Override
    public List<BonusRule> convertToEntityAttribute(String dbData) {
        if (dbData == null) return List.of();
        try {
            return MAPPER.readValue(dbData, TYPE_REF);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize bonus rules: " + dbData, e);
        }
    }
}
