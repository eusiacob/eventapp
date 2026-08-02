package com.example.eventapp.service;

import com.example.eventapp.model.UserNotification;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserNotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;

    public UserNotificationService(
            UserNotificationRepository userNotificationRepository
    ) {
        this.userNotificationRepository = userNotificationRepository;
    }

    public void create(
            User user,
            String title,
            String message,
            String link
    ){

        UserNotification notification =
                new UserNotification();

        notification.setUser(user);

        notification.setTitle(title);

        notification.setMessage(message);

        notification.setLink(link);

        userNotificationRepository.save(notification);
    }

    public List<UserNotification> getUserNotifications(User user) {

        return userNotificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {

        return userNotificationRepository
                .countByUserAndReadFalse(user);
    }

    public String markAsRead(
            Long notificationId,
            User user
    ) {


        UserNotification notification =
                userNotificationRepository
                        .findByIdAndUser(
                                notificationId,
                                user
                        )
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Access denied"
                                )
                        );


        notification.setRead(true);


        userNotificationRepository.save(notification);


        return notification.getLink();

    }

}
