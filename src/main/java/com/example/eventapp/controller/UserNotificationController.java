package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.UserNotificationService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final UserService userService;

    public UserNotificationController(
            UserNotificationService userNotificationService,
            UserService userService
    ) {
        this.userNotificationService = userNotificationService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public String openNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user =
                userService.findByEmail(userDetails.getUsername());
        String link =
                userNotificationService.markAsRead(id, user);

        return "redirect:" + link;
    }
}
