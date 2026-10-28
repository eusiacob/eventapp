package com.example.eventapp.controller;

import com.example.eventapp.dto.RegisterUserDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.service.EmailService;
import com.example.eventapp.service.EmailVerificationService;
import com.example.eventapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            UserService userService,
            EmailService emailService,
            EmailVerificationService emailVerificationService
    ) {
        this.userService = userService;
        this.emailService = emailService;
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "redirect:/?auth=register";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/?auth=login";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") RegisterUserDTO userDTO,
            BindingResult result,
            Model model
    ) {

        if (userService.emailExists(userDTO.getEmail())) {

            result.rejectValue(
                    "email",
                    "error.user",
                    "Adresa de email a mai fost folosită."
            );
        }

        if (!userDTO.getPassword()
                .equals(userDTO.getConfirmPassword())) {

            result.rejectValue(
                    "confirmPassword",
                    "error.user",
                    "Parolele nu se potrivesc."
            );
        }

        if (result.hasErrors()) {
            model.addAttribute("authModalMode", "register");
            return "home";
        }

        User user = userService.registerUser(userDTO);
        emailService.sendAccountCreatedEmail(
                userDTO.getEmail(),
                userDTO.getFirstName()
        );
        emailVerificationService.requestUserEmailVerification(user);

        return "redirect:/?auth=login&registered";
    }

}
