package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.UserNotificationService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;
    private final UserNotificationService userNotificationService;

    public GlobalModelAttributes(
            UserNotificationService userNotificationService,
            UserService userService
    ) {

        this.userService = userService;
        this.userNotificationService = userNotificationService;
    }

    @ModelAttribute("favoriteCount")
    public int favoriteCount(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }

        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return 0;
        }

        User user = userService.findByEmail(authentication.getName());

        if (user.getFavoriteBusinesses() == null) {
            return 0;
        }

        return user.getFavoriteBusinesses().size();
    }

    @ModelAttribute
    public void addNotifications(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails != null) {

            User user =
                    userService.findByEmail(userDetails.getUsername());

            model.addAttribute(
                    "notifications",
                    userNotificationService.getUserNotifications(user));

            model.addAttribute(
                    "notificationCount",
                    userNotificationService.getUnreadCount(user));
        }
    }
}