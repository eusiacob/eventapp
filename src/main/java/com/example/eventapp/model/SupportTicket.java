package com.example.eventapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String subject;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportCategory category;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    public enum SupportCategory {
        GENERAL,
        CONT,
        SERVICII,
        ABONAMENTE,
        PLATA,
        TEHNIC,
        ALTELE
    }


    public enum SupportStatus {
        OPEN,
        IN_PROGRESS,
        CLOSED
    }


    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = SupportStatus.OPEN;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

}