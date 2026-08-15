package com.example.eventapp.service;

import com.example.eventapp.model.Role;
import com.example.eventapp.model.Subscription;
import com.example.eventapp.model.SubscriptionPlan;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.SubscriptionPlanRepository;
import com.example.eventapp.repository.SubscriptionRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserNotificationService userNotificationService;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserNotificationService userNotificationService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userNotificationService = userNotificationService;
    }

    public Subscription findActiveSubscription(User user) {

        return subscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(
                        user,
                        Subscription.SubscriptionStatus.ACTIVE
                )
                .orElse(null);
    }

    public Subscription findById(Long id) {

        return subscriptionRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription not found"
                        )
                );
    }

    public void activateSubscription(Subscription subscription) {

        if (subscription.getStatus()
                != Subscription.SubscriptionStatus.PENDING) {

            throw new IllegalStateException(
                    "Subscription is not pending"
            );
        }

        User user = subscription.getUser();

        Subscription activeSubscription =
                findActiveSubscription(user);

        if (activeSubscription != null) {

            throw new IllegalStateException(
                    "User already has an active subscription"
            );
        }

        SubscriptionPlan plan =
                subscription.getPlan();

        LocalDateTime now =
                LocalDateTime.now();

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(now);

        switch (plan.getDuration()) {

            case MONTHLY -> subscription.setEndDate(
                    now.plusMonths(1)
            );

            case SIX_MONTHS -> subscription.setEndDate(
                    now.plusMonths(6)
            );

            case YEARLY -> subscription.setEndDate(
                    now.plusYears(1)
            );

            case TWO_YEARS -> subscription.setEndDate(
                    now.plusYears(2)
            );
        }

        user.setRole(Role.BUSINESS);
        userRepository.save(user);
        subscriptionRepository.save(subscription);
        userNotificationService.create(
                user,
                "Abonamentul tău a fost activat",
                "Abonamentul " + plan.getType() +
                        " (" + plan.getDuration().getDisplayName() +
                        ") este acum activ.",
                "/profile"
        );
    }

    public void createSubscription(User user, Long planId) {

        List<Subscription> userSubscriptions =
                subscriptionRepository
                        .findAllByUserOrderByCreatedAtDesc(user);

        boolean hasPending =
                userSubscriptions.stream()
                        .anyMatch(subscription ->
                                subscription.getStatus()
                                        == Subscription.SubscriptionStatus.PENDING
                        );

        if (hasPending) {

            throw new IllegalStateException(
                    "User already has a pending subscription"
            );
        }

        SubscriptionPlan plan =
                subscriptionPlanRepository
                        .findById(planId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subscription plan not found"
                                )
                        );

        if (!plan.isActive()) {

            throw new IllegalStateException(
                    "Subscription plan is not active"
            );
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setPrice(plan.getPrice());
        subscription.setStatus(
                Subscription.SubscriptionStatus.PENDING
        );

        subscriptionRepository.save(subscription);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void checkExpiredSubscriptions() {

        List<Subscription> subscriptions =
                subscriptionRepository
                        .findAllByStatusOrderByCreatedAtDesc(
                                Subscription.SubscriptionStatus.ACTIVE
                        );

        LocalDateTime now = LocalDateTime.now();

        for (Subscription subscription : subscriptions) {

            if (subscription.getEndDate() != null
                    && subscription.getEndDate().isBefore(now)) {

                subscription.setStatus(
                        Subscription.SubscriptionStatus.EXPIRED
                );

                subscriptionRepository.save(subscription);

                User user = subscription.getUser();

                userNotificationService.create(
                        user,
                        "Abonamentul tău a expirat",
                        "Abonamentul " + subscription.getPlan().getType()
                                + " (" + subscription.getPlan()
                                .getDuration()
                                .getDisplayName()
                                + ") a expirat.",
                        "/profile"
                );

                Subscription nextSubscription =
                        subscriptionRepository
                                .findAllByUserOrderByCreatedAtDesc(user)
                                .stream()
                                .filter(s ->
                                        s.getStatus()
                                                == Subscription.SubscriptionStatus.PENDING
                                )
                                .findFirst()
                                .orElse(null);

                if (nextSubscription != null) {

                    activateSubscription(nextSubscription);

                } else {

                    if (user.getRole() == Role.BUSINESS) {

                        user.setRole(Role.USER);

                        userRepository.save(user);
                    }
                }
            }
        }
    }

    public List<Subscription> findAll() {

        return subscriptionRepository
                .findAllByOrderByCreatedAtDesc();
    }

    public SubscriptionPlan findPlanById(Long id) {

        return subscriptionPlanRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"
                        )
                );
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

    public List<Subscription> findAllByUser(User user) {

        return subscriptionRepository
                .findAllByUserOrderByCreatedAtDesc(user);
    }


    public List<SubscriptionPlan> getAvailablePlans() {

        return subscriptionPlanRepository.findByActiveTrue();

    }

    public List<SubscriptionPlan> findAllPlans() {

        return subscriptionPlanRepository.findAll();
    }

    public SubscriptionPlan findActivePlanById(Long id) {

        SubscriptionPlan plan =
                subscriptionPlanRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subscription plan not found"
                                )
                        );

        if (!plan.isActive()) {

            throw new IllegalStateException(
                    "Subscription plan is not active"
            );
        }

        return plan;
    }

    public Subscription findPendingSubscription(User user) {

        return subscriptionRepository
                .findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(subscription ->
                        subscription.getStatus()
                                == Subscription.SubscriptionStatus.PENDING
                )
                .findFirst()
                .orElse(null);
    }

    public void updatePlan(
            Long id,
            BigDecimal price,
            boolean active
    ) {

        SubscriptionPlan plan =
                subscriptionPlanRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subscription plan not found"
                                )
                        );

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Invalid price"
            );
        }

        plan.setPrice(price);
        plan.setActive(active);

        subscriptionPlanRepository.save(plan);
    }

}
