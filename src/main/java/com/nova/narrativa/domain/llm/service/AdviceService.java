package com.nova.narrativa.domain.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.domain.llm.dto.AdviceResponse;
import com.nova.narrativa.domain.llm.dto.NPCChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AdviceService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AdviceService.class);

    @Value("${environments.narrativa-ml.url}")
    private String mlServerUrl;

    @Value("${environments.narrativa-ml.api-key}")
    private String apiKey;

    @Autowired
    public AdviceService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<AdviceResponse> getNpcAdvice(Long gameId) {
        return webClient.post()
                .uri(mlServerUrl + "/api/story/advice")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("game_id", gameId.toString()))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                        AdviceResponse adviceResponse = new AdviceResponse();
                        adviceResponse.setNpcMessage((String) response.get("npc_message"));
                        adviceResponse.setGameId(gameId.toString());
                        return adviceResponse;
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing NPC advice response: " + e.getMessage());
                    }
                });
    }

    public Mono<NPCChatResponse> chatWithNpc(Long gameId) {
        return webClient.post()
                .uri(mlServerUrl + "/api/story/chat")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("game_id", gameId.toString()))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        NPCChatResponse chatResponse = objectMapper.readValue(responseBody, NPCChatResponse.class);
                        chatResponse.setGameId(gameId.toString());
                        return chatResponse;
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing NPC chat response: " + e.getMessage());
                    }
                });
    }
}