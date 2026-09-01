package com.example.eventapp.config;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public CustomAuthenticationFailureHandler(
            UserRepository userRepository,
            EncryptionService encryptionService
    ) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {

        /*
         * CONT DEZACTIVAT
         */
        if (exception instanceof DisabledException) {

            response.sendRedirect("/login?disabled");

            return;
        }

        if (exception instanceof LockedException) {

            response.sendRedirect("/login?locked");

            return;
        }

        /*
         * EMAILUL INTRODUS LA LOGIN
         */
        String email = request.getParameter("username");

        if (email == null || email.isBlank()) {

            response.sendRedirect("/login?error");

            return;
        }

        email = email.trim().toLowerCase();

        String emailHash = encryptionService.hash(email);

        User user = userRepository
                .findByEmailHash(emailHash)
                .orElse(null);

        /*
         * Dacă utilizatorul nu există,
         * nu oferim informații despre existența contului.
         */
        if (user == null) {

            response.sendRedirect("/login?error");

            return;
        }

        LocalDateTime now = LocalDateTime.now();

        /*
         * VERIFICĂM DACĂ UTILIZATORUL ESTE
         * ÎNCĂ BLOCAT TEMPORAR
         */
        if (user.getLoginBlockedUntil() != null) {

            if (user.getLoginBlockedUntil().isAfter(now)) {

                response.sendRedirect("/login?locked");

                return;
            }

            /*
             * Blocarea a expirat.
             * Resetăm contorul.
             */
            user.setLoginBlockedUntil(null);
            user.setFailedLoginAttempts(0);

        }

        /*
         * CREȘTEM NUMĂRUL DE ÎNCERCĂRI EȘUATE
         */
        int attempts = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        /*
         * LA A 5-A ÎNCERCARE:
         * BLOCĂM CONTUL PENTRU 2 MINUTE
         */
        if (attempts >= 5) {

            user.setLoginBlockedUntil(
                    now.plusMinutes(2)
            );

            userRepository.save(user);

            response.sendRedirect("/login?locked");

            return;
        }

        /*
         * SALVĂM NUMĂRUL DE ÎNCERCĂRI
         */
        userRepository.save(user);

        response.sendRedirect("/login?error");
    }
}