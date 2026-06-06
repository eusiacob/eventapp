package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;

    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
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
}