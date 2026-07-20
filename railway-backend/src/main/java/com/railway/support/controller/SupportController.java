package com.railway.support.controller;

import com.railway.identity.entity.User;
import com.railway.identity.repository.UserRepository;
import com.railway.support.entity.SupportTicket;
import com.railway.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.railway.support.dto.SupportTicketRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(
            @Valid @RequestBody SupportTicketRequest request,
            Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .subject(request.getSubject())
                .description(request.getDescription())
                .pnrReference(request.getPnrReference())
                .status(SupportTicket.TicketStatus.OPEN)
                .build();

        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    @GetMapping("/tickets/my")
    public ResponseEntity<List<SupportTicket>> getMyTickets(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> getTicket(@PathVariable Long id, Authentication auth) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return ResponseEntity.ok(ticket);
    }
}
