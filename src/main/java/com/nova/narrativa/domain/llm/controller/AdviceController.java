package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.AdviceRequest;
import com.nova.narrativa.domain.llm.dto.AdviceResponse;
import com.nova.narrativa.domain.llm.dto.NPCChatResponse;
import com.nova.narrativa.domain.llm.service.AdviceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/npc")
public class AdviceController {

    private final AdviceService adviceService;
    private static final Logger logger = LoggerFactory.getLogger(AdviceController.class);

    @Autowired
    public AdviceController(AdviceService adviceService) {
        this.adviceService = adviceService;
    }

    @PostMapping("/advice")
    public Mono<AdviceResponse> getNpcAdvice(@Valid @RequestBody AdviceRequest request) {
        return adviceService.getNpcAdvice(request.getGameId());
    }

    @PostMapping("/chat")
    public Mono<NPCChatResponse> chatWithNpc(@Valid @RequestBody AdviceRequest request) {
        return adviceService.chatWithNpc(request.getGameId());
    }
}