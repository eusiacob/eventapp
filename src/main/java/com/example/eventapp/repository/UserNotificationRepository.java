package com.example.eventapp.repository;

import com.example.eventapp.model.UserNotification;
import com.example.eventapp.model.User;
import com.example.eventapp.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository
        extends JpaRepository<UserNotification, Long> {

    List<UserNotification>
    findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

}