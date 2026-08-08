package com.example.eventapp.config;

import com.example.eventapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();

        userRepository.findByEmail(email)
                .ifPresent(user -> {

                    user.setLastActivityAt(LocalDateTime.now());

                    userRepository.save(user);

                });

        response.sendRedirect("/businesses");
    }
}
