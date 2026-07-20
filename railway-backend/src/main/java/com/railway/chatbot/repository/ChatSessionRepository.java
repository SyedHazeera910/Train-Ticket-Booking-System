package com.railway.chatbot.repository;

import com.railway.chatbot.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    List<ChatSession> findByUserEmailOrderByUpdatedAtDesc(String userEmail);
}
