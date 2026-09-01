package com.example.eventapp.controller;

import com.example.eventapp.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(
            PasswordResetService passwordResetService
    ) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot")
    public String showForgotPasswordPage() {

        return "forgot";
    }

    @PostMapping("/forgot")
    public String processForgotPassword(
            @RequestParam String email
    ) {

        passwordResetService.requestPasswordReset(email);

        return "redirect:/forgot?successForgot";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam String token,
            Model model
    ) {

        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword
    ) {

        if (!password.equals(confirmPassword)) {
            return "redirect:/reset-password?token="
                    + token
                    + "&error=passwordMismatch";
        }

        boolean success = passwordResetService.resetPassword(
                token,
                password
        );

        if (!success) {
            return "redirect:/reset-password?error=invalidToken";
        }

        return "redirect:/login?resetSuccess";
    }
}