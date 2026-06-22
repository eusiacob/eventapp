package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserService userService;
    private final BusinessProfileService businessProfileService;

    public ProfileController(UserService userService,
                             BusinessProfileService businessProfileService) {
        this.userService = userService;
        this.businessProfileService = businessProfileService;
    }

    @GetMapping("/profile")
    public String profile(Model model,
                          @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        int favoriteCount = user.getFavoriteBusinesses() != null
                ? user.getFavoriteBusinesses().size()
                : 0;

        int businessCount = businessProfileService.findByUser(user).size();

        model.addAttribute("user", user);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("businessCount", businessCount);

        return "profile";
    }
}