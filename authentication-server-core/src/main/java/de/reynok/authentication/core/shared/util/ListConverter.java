package de.reynok.authentication.core.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ListConverter implements AttributeConverter<List<String>, String> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> stringStringMap) {
        try {
            return objectMapper.writeValueAsString(stringStringMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to process list to JSON due to {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        TypeReference<List<String>> reference = new TypeReference<List<String>>() {};
        try {
            return objectMapper.readValue(s, reference);
        } catch (IOException e) {
            log.error("Failed to read from json due to {}", e.getMessage(), e);
            return null;
        }
    }
}
