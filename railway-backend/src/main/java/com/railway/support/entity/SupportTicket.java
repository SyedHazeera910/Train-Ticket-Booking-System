package com.railway.support.entity;

import com.railway.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "support_tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column
    private String pnrReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); this.updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }

    public enum TicketStatus { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
}
