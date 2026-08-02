package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.UserNotificationService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserNotificationService userNotificationService;
    private final UserService userService;

    public GlobalControllerAdvice(
            UserNotificationService userNotificationService,
            UserService userService
    ) {
        this.userNotificationService = userNotificationService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addNotifications(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails != null) {

            User user =
                    userService.findByEmail(
                            userDetails.getUsername()
                    );

            model.addAttribute(
                    "notifications",
                    userNotificationService
                            .getUserNotifications(user)
            );

            model.addAttribute(
                    "notificationCount",
                    userNotificationService
                            .getUnreadCount(user)
            );
        }
    }
}
