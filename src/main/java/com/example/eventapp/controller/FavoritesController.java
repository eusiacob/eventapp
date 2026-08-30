package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoritesController {

    private final UserService userService;
    private final UserRepository userRepository;

    public FavoritesController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/favorites")
    public String favorites(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User user =
                userService.findByEmail(userDetails.getUsername());

        model.addAttribute("favorites",user.getFavoriteBusinesses());

        model.addAttribute(
                "breadcrumbs",
                List.of(
                        new BreadcrumbDTO(
                                "Acasă",
                                "/businesses"
                        ),
                        new BreadcrumbDTO(
                                "Favorite",
                                null
                        )
                )
        );

        return "favorites";
    }

    @PostMapping("/favorites/add/{id}")
    public String addFavorite(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestHeader(value = "Referer", required = false) String referer) {

        userService.addFavorite(id, userDetails.getUsername());

        return "redirect:" + (referer != null ? referer : "/businesses");
    }

    @PostMapping("/favorites/remove/{uuid}")
    public String removeFavorite(@PathVariable String uuid,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @RequestHeader(value = "Referer", required = false) String referer) {

        userService.removeFavorite(uuid, userDetails.getUsername());

        return "redirect:" + (referer != null ? referer : "/businesses");
    }

    @PostMapping("/favorites/toggle/{businessUuid}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable String businessUuid,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        boolean isFavorite = userService.toggleFavorite(businessUuid, userDetails.getUsername());

        User user = userService.findByEmail(userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("favorite", isFavorite);
        response.put("favoriteCount", user.getFavoriteBusinesses().size());

        return response;
    }
}
