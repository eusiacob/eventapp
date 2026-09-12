package com.example.eventapp.controller;

import com.example.eventapp.dto.LegalAcceptanceDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LegalAcceptanceController {

    private final UserService userService;

    public LegalAcceptanceController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/legal-acceptance")
    public String showLegalAcceptance(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userService.findByEmail(userDetails.getUsername());

        if (userService.hasCurrentLegalAcceptances(user)) {
            return "redirect:/profile";
        }

        model.addAttribute("acceptance", new LegalAcceptanceDTO());
        return "legal-acceptance";
    }

    @PostMapping("/profile/legal-acceptance")
    public String acceptLegalDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("acceptance") LegalAcceptanceDTO acceptance,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "legal-acceptance";
        }

        User user = userService.findByEmail(userDetails.getUsername());
        userService.acceptCurrentLegalDocuments(user, acceptance);

        return "redirect:/businesses";
    }
}
