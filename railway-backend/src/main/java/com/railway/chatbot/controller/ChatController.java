package com.railway.chatbot.controller;

import com.railway.chatbot.dto.ChatRequest;
import com.railway.chatbot.dto.ChatResponse;
import com.railway.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    /**
     * POST /api/chatbot/message
     * Requires a valid JWT Bearer token in the Authorization header.
     *
     * Body: { "sessionId": "client-uuid", "message": "user text" }
     * Returns: { "reply": "...", "intentType": "...", "sessionId": "..." }
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication auth) {
        String userEmail = (auth != null && auth.getName() != null) ? auth.getName() : "guest@railways.com";
        ChatResponse response = chatbotService.handleMessage(userEmail, request);
        return ResponseEntity.ok(response);
    }
}
