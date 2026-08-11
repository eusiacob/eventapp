package com.example.eventapp.service;

import com.example.eventapp.dto.SubscriptionPlanDTO;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.Subscription;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.SubscriptionRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public Subscription findByUser(User user) {

        return subscriptionRepository.findByUser(user).orElse(null);
    }

    public void activateSubscription(
            User user,
            Subscription.SubscriptionPlan plan) {

        Subscription subscription =
                subscriptionRepository
                        .findByUser(user)
                        .orElse(new Subscription());

        subscription.setUser(user);
        subscription.setPlan(plan);

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        LocalDateTime now = LocalDateTime.now();

        subscription.setStartDate(now);

        switch (plan) {

            case MONTHLY ->
                    subscription.setEndDate(
                            now.plusMonths(1)
                    );

            case SIXMONTHS ->
                    subscription.setEndDate(
                            now.plusMonths(6)
                    );

            case YEARLY ->
                    subscription.setEndDate(
                            now.plusYears(1)
                    );
        }

        user.setRole(Role.BUSINESS);

        userRepository.save(user);

        subscriptionRepository.save(subscription);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void checkExpiredSubscriptions() {

        List<Subscription> subscriptions = subscriptionRepository.
                findByStatus(Subscription.SubscriptionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();

        for (Subscription subscription : subscriptions) {

            if (subscription.getEndDate().isBefore(now)) {

                subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);

                User user = subscription.getUser();

                if (user.getRole() == Role.BUSINESS) {

                    user.setRole(Role.USER);
                    userRepository.save(user);

                }

                subscriptionRepository.save(subscription);

            }

        }

    }

    public List<Subscription> findAll() {

        return subscriptionRepository
                .findAllByOrderByCreatedAtDesc();
    }

    public long count() {

        return subscriptionRepository.count();

    }

    public List<Subscription> findByStatus(
            Subscription.SubscriptionStatus status
    ) {

        return subscriptionRepository
                .findAllByStatusOrderByCreatedAtDesc(status);

    }

    public List<SubscriptionPlanDTO> getAvailablePlans() {

        return List.of(

                new SubscriptionPlanDTO(
                        Subscription.SubscriptionPlan.MONTHLY,
                        "Lunar",
                        new BigDecimal("49.00"),
                        1,
                        "Flexibilitate maximă"
                ),

                new SubscriptionPlanDTO(
                        Subscription.SubscriptionPlan.SIXMONTHS,
                        "6 luni",
                        new BigDecimal("249.00"),
                        6,
                        "Economisești față de plata lunară"
                ),

                new SubscriptionPlanDTO(
                        Subscription.SubscriptionPlan.YEARLY,
                        "Anual",
                        new BigDecimal("449.00"),
                        12,
                        "Cea mai bună valoare"
                )
        );
    }

    public void createSubscription(
            User user,
            Subscription.SubscriptionPlan plan
    ) {

        Optional<Subscription> existingSubscription =
                subscriptionRepository.findByUser(user);

        if (existingSubscription.isPresent()) {
            throw new IllegalStateException(
                    "User already has a subscription"
            );
        }

        Subscription subscription = new Subscription();

        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(Subscription.SubscriptionStatus.PENDING);

        subscriptionRepository.save(subscription);
    }

}
