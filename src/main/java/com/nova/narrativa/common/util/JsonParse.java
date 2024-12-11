package com.nova.narrativa.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParse {

    private static ObjectMapper mapper = new ObjectMapper();

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }

    public static JsonNode parse(String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }
}