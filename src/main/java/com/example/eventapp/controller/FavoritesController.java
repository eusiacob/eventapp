package com.example.eventapp.controller;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FavoritesController {

    private final UserService userService;
    private final UserRepository userRepository;

    public FavoritesController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;

    }

    @GetMapping("/favorites")
    public String favorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        model.addAttribute("favorites", user.getFavoriteBusinesses());

        return "favorites";
    }

    @PostMapping("/favorites/add/{id}")
    public String addFavorite(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        userService.addFavorite(id, userDetails.getUsername());

        return "redirect:/business/" + id;
    }

    @PostMapping("/favorites/remove/{id}")
    public String removeFavorite(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        userService.removeFavorite(id, userDetails.getUsername());

        return "redirect:/favorites";
    }

}
