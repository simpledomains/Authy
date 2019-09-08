package de.reynok.authentication.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class HashMapConverter implements AttributeConverter<Map<String, String>, String> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> stringStringMap) {
        try {
            return objectMapper.writeValueAsString(stringStringMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to process hashMap to JSON due to {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String s) {
        TypeReference<Map<String, String>> reference = new TypeReference<Map<String, String>>() {};
        try {
            return objectMapper.readValue(s, reference);
        } catch (IOException e) {
            log.error("Failed to read from json due to {}", e.getMessage(), e);
            return null;
        }
    }
}