package com.example.eventapp.service;

import com.example.eventapp.model.*;
import com.example.eventapp.repository.UserNotificationRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    public UserNotificationService(
            UserNotificationRepository userNotificationRepository,
            UserRepository userRepository
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.userRepository = userRepository;
    }

    public void create(
            User user,
            String title,
            String message,
            String link
    ){

        UserNotification notification = new UserNotification();

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
                        .findByIdAndUser(notificationId, user)
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Access denied"));

        notification.setRead(true);
        userNotificationRepository.save(notification);

        return notification.getLink();

    }

    public void notifyAdminsNewReview(Review review) {

        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            UserNotification notification = new UserNotification();

            notification.setUser(admin);
            notification.setTitle("Recenzie nouă");
            notification.setMessage(
                    "A fost adăugată o recenzie nouă pentru serviciul \"" +
                            review.getBusinessProfile().getName() +
                            "\" și așteaptă aprobare."
            );

            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notification.setLink("/admin/review/" + review.getId());
            userNotificationRepository.save(notification);
        }
    }

    public void notifyReviewApproved(Review review) {

        UserNotification notification = new UserNotification();

        notification.setUser(review.getUser());

        notification.setTitle("Recenzie aprobată");

        notification.setMessage(
                "Recenzia ta pentru \"" +
                        review.getBusinessProfile().getName() +
                        "\" a fost aprobată și este acum vizibilă public."
        );

        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setLink("/business/" + review.getBusinessProfile().getUuid());
        userNotificationRepository.save(notification);
    }

    public void notifyReviewRejected(Review review) {

        UserNotification notification = new UserNotification();
        notification.setUser(review.getUser());
        notification.setTitle("Recenzie respinsă");
        String message =
                "Recenzia ta pentru \"" +
                        review.getBusinessProfile().getName() +
                        "\" a fost respinsă.";

        if (review.getRejectionReason() != null &&
                !review.getRejectionReason().isBlank()) {

            message += "\n\nMotiv: " + review.getRejectionReason();
        }

        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        userNotificationRepository.save(notification);
    }

    public void notifyAdminsNewSupportTicket(
            SupportTicket ticket
    ) {

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            UserNotification notification =
                    new UserNotification();

            notification.setUser(admin);

            notification.setTitle(
                    "Solicitare Support nouă"
            );

            notification.setMessage(
                    "Utilizatorul " +
                            ticket.getUser().getFirstName() +
                            " " +
                            ticket.getUser().getLastName() +
                            " a deschis o solicitare: \"" +
                            ticket.getSubject() +
                            "\"."
            );

            notification.setRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now()
            );

            notification.setLink(
                    "/admin/support/" +
                            ticket.getId()
            );

            userNotificationRepository.save(
                    notification
            );
        }
    }

    public void notifyAdminsNewSupportMessage(
            SupportTicket ticket
    ) {

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            UserNotification notification =
                    new UserNotification();

            notification.setUser(admin);

            notification.setTitle(
                    "Mesaj nou în Support"
            );

            notification.setMessage(
                    "Utilizatorul " +
                            ticket.getUser().getFirstName() +
                            " " +
                            ticket.getUser().getLastName() +
                            " a trimis un mesaj nou în solicitarea \"" +
                            ticket.getSubject() +
                            "\"."
            );

            notification.setRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now()
            );

            notification.setLink(
                    "/admin/support/" +
                            ticket.getId()
            );

            userNotificationRepository.save(
                    notification
            );
        }
    }


}
