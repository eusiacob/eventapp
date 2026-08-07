package com.example.eventapp.service;

import com.example.eventapp.model.Role;
import com.example.eventapp.model.Subscription;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.SubscriptionRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public Subscription findByUser(User user) {

        return subscriptionRepository.findByUser(user)
                .orElse(null);
    }

    public Subscription activateSubscription(
            User user,
            Subscription.SubscriptionPlan plan
    ) {

        Subscription subscription =
                subscriptionRepository.findByUser(user)
                        .orElse(new Subscription());

        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );


        LocalDateTime now = LocalDateTime.now();

        subscription.setStartDate(now);


        if (plan == Subscription.SubscriptionPlan.MONTHLY) {

            subscription.setEndDate(
                    now.plusMonths(1)
            );

        } else {

            subscription.setEndDate(
                    now.plusYears(1)
            );

        }

        user.setRole(Role.BUSINESS);
        userRepository.save(user);
        return subscriptionRepository.save(subscription);

    }

}
