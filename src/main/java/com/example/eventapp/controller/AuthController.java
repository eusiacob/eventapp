package com.example.eventapp.controller;

import com.example.eventapp.dto.RegisterUserDTO;
import com.example.eventapp.model.AccountStatusReason;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") RegisterUserDTO userDTO,
            BindingResult result
    ) {

        if (userService.emailExists(userDTO.getEmail())) {

            result.rejectValue(
                    "email",
                    "error.user",
                    "Email already exists"
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
            return "register";
        }

        userService.registerUser(userDTO);

        return "redirect:/login?registered";
    }

}