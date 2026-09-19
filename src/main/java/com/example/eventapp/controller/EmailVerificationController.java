package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.EmailVerificationService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    private final BusinessProfileService businessProfileService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService,
            UserService userService,
            BusinessProfileService businessProfileService
    ) {
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
        this.businessProfileService = businessProfileService;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(required = false) String token,
                              Model model,
                              HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        model.addAttribute("verified", emailVerificationService.verify(token));
        return "email-verification-result";
    }

    @PostMapping("/profile/email-verification")
    public String resendUserEmailVerification(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        User user = userService.findByEmail(userDetails.getUsername());

        if (user.isEmailVerified()) {
            redirectAttributes.addFlashAttribute(
                    "accountSuccess",
                    "Emailul contului este deja verificat."
            );

            return "redirect:/profile";
        }

        emailVerificationService.requestUserEmailVerification(user);

        redirectAttributes.addFlashAttribute(
                "accountSuccess",
                "Ți-am trimis un nou link de verificare pe email."
        );

        return "redirect:/profile";
    }

    @PostMapping("/business/{uuid}/email-verification")
    public String resendBusinessEmailVerification(
            @PathVariable String uuid,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile businessProfile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );

        if (businessProfile.getEmail() == null ||
                businessProfile.getEmail().isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Adaugă un email pentru serviciu înainte de verificare."
            );

            return "redirect:/business/edit/" + uuid;
        }

        if (businessProfile.isEmailVerified()) {
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Emailul serviciului este deja verificat."
            );

            return "redirect:/business/edit/" + uuid;
        }

        emailVerificationService
                .requestBusinessEmailVerification(businessProfile);

        redirectAttributes.addFlashAttribute(
                "success",
                "Am trimis un link de verificare către emailul serviciului."
        );

        return "redirect:/business/edit/" + uuid;
    }
}
