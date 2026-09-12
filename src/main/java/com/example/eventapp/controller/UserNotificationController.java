package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.service.UserNotificationService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    @GetMapping
    public String notifications(
            @RequestParam(defaultValue = "false") boolean unread,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userService.findByEmail(userDetails.getUsername());

        model.addAttribute(
                "notificationPageItems",
                unread
                        ? userNotificationService.getUnreadNotifications(user)
                        : userNotificationService.getUserNotifications(user)
        );
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Notificări", null)));
        model.addAttribute("showUnreadOnly", unread);

        return "notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userService.findByEmail(userDetails.getUsername());
        userNotificationService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute(
                "notificationSuccess",
                "Toate notificările au fost marcate ca citite."
        );

        return "redirect:/notifications";
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
