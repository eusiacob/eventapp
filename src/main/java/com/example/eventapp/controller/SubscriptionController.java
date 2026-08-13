package com.example.eventapp.controller;

import com.example.eventapp.model.SubscriptionPlan;
import com.example.eventapp.model.User;
import com.example.eventapp.service.SubscriptionService;
import com.example.eventapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;


    @GetMapping("/subscriptions")
    public String subscriptionPlans(Model model) {

        model.addAttribute(
                "plans",
                subscriptionService.getAvailablePlans()
        );

        return "subscriptions";
    }

    @GetMapping("/subscriptions/confirm")
    public String confirmSubscription(
            @RequestParam Long planId,
            Model model
    ) {

        SubscriptionPlan plan = subscriptionService.findActivePlanById(planId);

        model.addAttribute("plan", plan);

        return "subscription-confirm";
    }

    @PostMapping("/subscriptions/create")
    public String createSubscription(
            @RequestParam Long planId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User user = userService.findByEmail(userDetails.getUsername());

        subscriptionService.createSubscription(user, planId);

        return "redirect:/subscriptions";
    }
}
