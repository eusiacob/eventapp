package com.example.eventapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionDuration duration;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public enum SubscriptionType {

        STANDARD,
        PREMIUM

    }

    @Getter
    public enum SubscriptionDuration {

        MONTHLY ("1 lună"),
        SIX_MONTHS ("6 luni"),
        YEARLY ("1 an"),
        TWO_YEARS ("2 ani");

        private final String displayName;

        SubscriptionDuration(String displayName) {
            this.displayName = displayName;
        }
    }
    
}
