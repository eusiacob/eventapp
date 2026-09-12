package com.example.eventapp.repository;

import com.example.eventapp.model.UserNotification;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository
        extends JpaRepository<UserNotification, Long> {

    List<UserNotification>
    findByUserOrderByCreatedAtDesc(User user);

    List<UserNotification>
    findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

    Optional<UserNotification> findByIdAndUser(
            Long id,
            User user
    );

    void deleteByUser(User user);
}
