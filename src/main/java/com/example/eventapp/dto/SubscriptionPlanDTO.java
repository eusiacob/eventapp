package com.example.eventapp.dto;

import com.example.eventapp.model.Subscription;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanDTO {

    private Subscription.SubscriptionPlan plan;

    private String name;

    private BigDecimal price;

    private int durationMonths;

    private String description;

    public SubscriptionPlanDTO(
            Subscription.SubscriptionPlan plan,
            String name,
            BigDecimal price,
            int durationMonths,
            String description
    ) {
        this.plan = plan;
        this.name = name;
        this.price = price;
        this.durationMonths = durationMonths;
        this.description = description;
    }
}
