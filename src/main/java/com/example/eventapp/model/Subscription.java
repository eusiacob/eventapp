package com.example.eventapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Getter
    public enum SubscriptionStatus {

        PENDING ("În așteptare"),
        ACTIVE ("Activ"),
        EXPIRED ("Expirat"),
        CANCELLED ("Anulat");

        private final String statusDisplayName;
        SubscriptionStatus(String statusDisplayName) {
            this.statusDisplayName =statusDisplayName;
        }

    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}