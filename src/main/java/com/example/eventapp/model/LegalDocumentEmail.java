package com.example.eventapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Durable pending delivery; no copies of user email addresses are stored here. */
@Entity
@Getter
@Setter
@Table(name = "legal_document_emails", indexes = {
        @Index(name = "idx_legal_email_due", columnList = "next_attempt_at,id")
})
public class LegalDocumentEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 32)
    private LegalDocumentType documentType;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(nullable = false)
    private LocalDate lastUpdated;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(nullable = false)
    private int attempts;
}
