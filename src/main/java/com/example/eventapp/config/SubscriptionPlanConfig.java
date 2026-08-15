package com.example.eventapp.config;

import com.example.eventapp.model.SubscriptionPlan;
import com.example.eventapp.repository.SubscriptionPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class SubscriptionPlanConfig {

    @Bean
    CommandLineRunner subscriptionPlans(
            SubscriptionPlanRepository repository
    ) {

        return args -> {

            if (repository.count() > 0) {
                return;
            }


            // STANDARD
            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.STANDARD,
                    SubscriptionPlan.SubscriptionDuration.MONTHLY,
                    "49.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.STANDARD,
                    SubscriptionPlan.SubscriptionDuration.SIX_MONTHS,
                    "249.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.STANDARD,
                    SubscriptionPlan.SubscriptionDuration.YEARLY,
                    "449.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.STANDARD,
                    SubscriptionPlan.SubscriptionDuration.TWO_YEARS,
                    "799.00"
            ));


            // PREMIUM
            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.PREMIUM,
                    SubscriptionPlan.SubscriptionDuration.MONTHLY,
                    "89.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.PREMIUM,
                    SubscriptionPlan.SubscriptionDuration.SIX_MONTHS,
                    "449.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.PREMIUM,
                    SubscriptionPlan.SubscriptionDuration.YEARLY,
                    "799.00"
            ));

            repository.save(createPlan(
                    SubscriptionPlan.SubscriptionType.PREMIUM,
                    SubscriptionPlan.SubscriptionDuration.TWO_YEARS,
                    "1399.00"
            ));

        };
    }


    private SubscriptionPlan createPlan(
            SubscriptionPlan.SubscriptionType type,
            SubscriptionPlan.SubscriptionDuration duration,
            String price
    ) {

        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setType(type);
        plan.setDuration(duration);
        plan.setPrice(new BigDecimal(price));
        plan.setActive(true);

        return plan;
    }

}