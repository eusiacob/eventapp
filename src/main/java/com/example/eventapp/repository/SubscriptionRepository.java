package com.example.eventapp.repository;

import com.example.eventapp.model.Subscription;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser(User user);

    List<Subscription> findByStatus(
            Subscription.SubscriptionStatus status
    );

    List<Subscription> findAllByOrderByCreatedAtDesc();

    List<Subscription> findAllByStatusOrderByCreatedAtDesc(
            Subscription.SubscriptionStatus status
    );

}
