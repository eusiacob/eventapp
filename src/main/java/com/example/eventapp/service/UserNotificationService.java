package com.example.eventapp.service;

import com.example.eventapp.model.UserNotification;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserNotificationService {

    private final UserNotificationRepository notificationRepository;

    public UserNotificationService(
            UserNotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    public void create(
            User user,
            String title,
            String message
    ) {

        UserNotification userNotification = new UserNotification();

        userNotification.setUser(user);
        userNotification.setTitle(title);
        userNotification.setMessage(message);

        notificationRepository.save(userNotification);

    }

    public List<UserNotification> getUserNotifications(User user) {

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);

    }

    public long getUnreadCount(User user) {

        return notificationRepository
                .countByUserAndReadFalse(user);

    }

}
