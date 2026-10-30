package com.example.eventapp.controller;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

final class AuthPageRedirect {
    private static final Set<String> FORWARDED_PARAMETERS = Set.of(
            "token", "error", "disabled", "locked", "registered", "emailVerified",
            "emailVerificationInvalid", "resetSuccess", "logout", "emailChanged",
            "accountDeleted", "successForgot"
    );

    private AuthPageRedirect() {
    }

    static String toPage(String auth, MultiValueMap<String, String> query) {
        String path = switch (auth == null ? "" : auth) {
            case "login" -> "/login";
            case "register" -> "/register";
            case "forgot" -> "/forgot";
            case "reset" -> "/reset-password";
            default -> "/businesses";
        };

        UriComponentsBuilder destination = UriComponentsBuilder.fromPath(path);
        query.forEach((name, values) -> {
            if (FORWARDED_PARAMETERS.contains(name)) {
                values.forEach(value -> {
                    if (value == null || value.isEmpty()) destination.queryParam(name);
                    else destination.queryParam(name, value);
                });
            }
        });
        return "redirect:" + destination.build().encode().toUriString();
    }
}
