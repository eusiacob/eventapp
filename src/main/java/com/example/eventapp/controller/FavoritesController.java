package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoritesController {

    private final UserService userService;

    public FavoritesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/favorites")
    public String favorites(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        model.addAttribute(
                "favorites",
                userService.getVisibleFavoriteBusinesses(userDetails.getUsername())
        );

        model.addAttribute(
                "breadcrumbs",
                List.of(new BreadcrumbDTO("Acasă", "/businesses"),
                        new BreadcrumbDTO("Profil", "/profile"),
                        new BreadcrumbDTO("Favorite", null)));

        return "favorites";
    }

    @PostMapping("/favorites/add/{id}")
    public String addFavorite(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestHeader(value = "Referer", required = false) String referer,
                              HttpServletRequest request) {

        userService.addFavorite(id, userDetails.getUsername());

        return "redirect:" + safeRedirectTarget(referer, request);
    }

    @PostMapping("/favorites/remove/{uuid}")
    public String removeFavorite(@PathVariable String uuid,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 HttpServletRequest request) {

        userService.removeFavorite(uuid, userDetails.getUsername());

        return "redirect:" + safeRedirectTarget(referer, request);
    }

    @PostMapping("/favorites/toggle/{businessUuid}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable String businessUuid,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        boolean isFavorite = userService.toggleFavorite(businessUuid, userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("favorite", isFavorite);
        response.put(
                "favoriteCount",
                userService.getVisibleFavoriteCount(userDetails.getUsername())
        );

        return response;
    }

    private String safeRedirectTarget(
            String referer,
            HttpServletRequest request
    ) {

        if (referer == null || referer.isBlank()) {
            return "/businesses";
        }

        try {
            URI uri = new URI(referer);

            if (!uri.isAbsolute()) {
                return safeLocalPath(uri.toString());
            }

            if (!isSameOrigin(uri, request)) {
                return "/businesses";
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery();

            return safeLocalPath(
                    path + (query != null ? "?" + query : "")
            );

        } catch (URISyntaxException e) {
            return "/businesses";
        }
    }

    private String safeLocalPath(String target) {

        if (target == null ||
                target.isBlank() ||
                !target.startsWith("/") ||
                target.startsWith("//") ||
                target.contains("\\") ||
                target.contains("\r") ||
                target.contains("\n")) {

            return "/businesses";
        }

        return target;
    }

    private boolean isSameOrigin(
            URI uri,
            HttpServletRequest request
    ) {

        String requestScheme = request.getScheme();
        String requestHost = request.getServerName();
        int requestPort = request.getServerPort();
        int uriPort = uri.getPort() == -1
                ? defaultPort(uri.getScheme())
                : uri.getPort();

        return requestScheme.equalsIgnoreCase(uri.getScheme())
                && requestHost.equalsIgnoreCase(uri.getHost())
                && requestPort == uriPort;
    }

    private int defaultPort(String scheme) {

        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }

        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }

        return -1;
    }
}
