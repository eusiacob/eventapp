package com.example.eventapp.service;

import com.example.eventapp.model.PasswordResetToken;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.PasswordResetTokenRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EncryptionService encryptionService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.emailService = emailService;
    }

    @Transactional
    public void requestPasswordReset(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        String emailHash =
                encryptionService.hash(normalizedEmail);

        User user = userRepository
                .findByEmailHash(emailHash)
                .orElse(null);

        /*
         * Nu spunem utilizatorului dacă emailul există.
         * Previne enumerarea conturilor.
         */
        if (user == null) {
            return;
        }

        /*
         * Invalidăm tokenurile anterioare.
         */
        tokenRepository.deleteByUser(user);

        /*
         * Generăm token aleatoriu criptografic sigur.
         */
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        /*
         * În DB salvăm doar hash-ul tokenului.
         */
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setTokenHash(tokenHash);
        resetToken.setUser(user);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(TOKEN_EXPIRATION_MINUTES)
        );
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        String resetLink =
                "http://localhost:8080/reset-password?token="
                        + rawToken;

        emailService.sendPasswordResetEmail(
                normalizedEmail,
                resetLink
        );

    }

    @Transactional
    public boolean resetPassword(
            String rawToken,
            String newPassword
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                tokenRepository
                        .findByTokenHashAndUsedFalse(tokenHash)
                        .orElse(null);

        if (resetToken == null) {
            return false;
        }

        /*
         * Verificăm expirarea.
         */
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            return false;
        }

        User user = resetToken.getUser();

        /*
         * Setăm noua parolă folosind BCrypt.
         */
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        /*
         * Resetăm și mecanismul de lockout.
         */
        user.setFailedLoginAttempts(0);
        user.setLoginBlockedUntil(null);

        userRepository.save(user);

        /*
         * Tokenul devine inutilizabil.
         */
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return true;
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                hex.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 indisponibil",
                    e
            );
        }
    }
}