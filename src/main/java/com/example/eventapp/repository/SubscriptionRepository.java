package com.example.eventapp.repository;

import com.example.eventapp.model.Subscription;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserAndStatusOrderByEndDateDesc(
            User user,
            Subscription.SubscriptionStatus status
    );

    List<Subscription> findAllByUserOrderByCreatedAtDesc(
            User user
    );

    List<Subscription> findAllByOrderByCreatedAtDesc();

    List<Subscription> findAllByStatusOrderByCreatedAtDesc(
            Subscription.SubscriptionStatus status
    );

}
