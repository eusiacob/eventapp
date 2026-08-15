package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.ReviewService;
import com.example.eventapp.service.SubscriptionService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final BusinessProfileService businessProfileService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;

    public ProfileController(UserService userService,
                             BusinessProfileService businessProfileService,
                             ReviewService reviewService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.businessProfileService = businessProfileService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/profile")
    public String profile(Model model,
                          @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        int favoriteCount = user.getFavoriteBusinesses() != null
                ? user.getFavoriteBusinesses().size() : 0;

        int businessCount = businessProfileService.findByUser(user).size();

        Long reviewCount = reviewService.countByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("activeSubscription",
                subscriptionService.findActiveSubscription(user));
        model.addAttribute("pendingSubscription",
                subscriptionService.findPendingSubscription(user));
        model.addAttribute("subscriptionHistory",
                subscriptionService.findAllByUser(user));
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("businessCount", businessCount);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Profil", null)));
        return "profile";
    }

    @GetMapping("/profile/ratings")
    public String userRatings(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("reviews", reviewService.findUserReviews(user));
        model.addAttribute("averageRating", reviewService.getUserAverageRating(user));
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Recenziile mele", null)));
        return "user-ratings";
    }
}