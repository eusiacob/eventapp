package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.UserNotificationService;
import com.example.eventapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public int favoriteCount(Authentication authentication, HttpServletRequest request) {

        if (isErrorRequest(request)) {
            return 0;
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }

        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return 0;
        }

        try {
            return userService.getVisibleFavoriteCount(authentication.getName());
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    @ModelAttribute
    public void addNotifications(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {

        if (isErrorRequest(request)) {
            return;
        }

        if (userDetails != null) {

            try {
                User user =
                        userService.findByEmail(userDetails.getUsername());

                model.addAttribute(
                        "notifications",
                        userNotificationService.getUserNotifications(user));

                model.addAttribute(
                        "notificationCount",
                        userNotificationService.getUnreadCount(user));
            } catch (RuntimeException exception) {
                model.addAttribute("notificationCount", 0);
            }
        }
    }

    private boolean isErrorRequest(HttpServletRequest request) {
        return request != null && request.getRequestURI() != null && request.getRequestURI().startsWith("/error");
    }
}
