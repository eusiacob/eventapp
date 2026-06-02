package com.example.eventapp.controller;

import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FavoriteController {


    private final UserService userService;


    public FavoriteController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/favorites/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        boolean isFavorite = userService.toggleFavorite(id, userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("favorite", isFavorite);

        return response;
    }
}
