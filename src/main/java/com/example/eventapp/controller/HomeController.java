package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final BusinessProfileService businessProfileService;

    private final UserService userService;

    public HomeController(BusinessProfileService businessProfileService, UserService userService) {
        this.businessProfileService = businessProfileService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/businesses")
    public String home(@RequestParam(required = false, defaultValue = "") String category,
                       @RequestParam(required = false, defaultValue = "") String city,
                       @RequestParam(required = false, defaultValue = "") String keyword,
                       Model model,
                       @AuthenticationPrincipal UserDetails userDetails
    ) {

        model.addAttribute("businesses", businessProfileService.search(category, city, keyword));

        model.addAttribute("categories", businessProfileService.getCategories());
        model.addAttribute("cities", businessProfileService.getCities());

        model.addAttribute("category", category);
        model.addAttribute("city", city);
        model.addAttribute("keyword", keyword);

        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());

            Set<Long> favoriteIds = user.getFavoriteBusinesses()
                    .stream()
                    .map(BusinessProfile::getId)
                    .collect(Collectors.toSet());

            model.addAttribute("favoriteIds", favoriteIds);
        } else {
            model.addAttribute("favoriteIds", Collections.emptySet());
        }

        return "businesses";
    }
}