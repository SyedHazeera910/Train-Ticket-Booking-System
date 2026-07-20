package com.railway.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatSession {

    @Id
    @Column(length = 64)
    private String sessionId;

    /** Email of the logged-in user (matches Spring Security principal name) */
    @Column(nullable = false)
    private String userEmail;

    /**
     * Rolling conversation history stored as a JSON array of turns.
     * Format: [{"role":"user","text":"..."},{"role":"bot","text":"..."},...]
     * We keep the last 20 turns to avoid unbounded growth.
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String historyJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.historyJson == null) this.historyJson = "[]";
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
