package com.railway.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    /** Client-generated UUID for this conversation. Frontend creates once per mount. */
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    /** The raw user message text */
    @NotBlank(message = "Message text is required")
    private String message;
}
