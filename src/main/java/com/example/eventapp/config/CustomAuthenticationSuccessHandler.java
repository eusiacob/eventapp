package com.example.eventapp.config;

import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.EncryptionService;
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
    private final EncryptionService encryptionService;

    public CustomAuthenticationSuccessHandler(
            UserRepository userRepository,
            EncryptionService encryptionService
    ) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String email =
                authentication.getName()
                        .trim()
                        .toLowerCase();

        String emailHash =
                encryptionService.hash(email);

        userRepository
                .findByEmailHash(emailHash)
                .ifPresent(user -> {

                    user.setLastActivityAt(
                            LocalDateTime.now()
                    );

                    user.setFailedLoginAttempts(0);
                    user.setLoginBlockedUntil(null);

                    userRepository.save(user);
                });

        response.sendRedirect("/businesses");
    }
}
