package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.EmailVerificationTargetType;
import com.example.eventapp.model.EmailVerificationToken;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.EmailVerificationTokenRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final EmailService emailService;
    private final EncryptionService encryptionService;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            BusinessProfileRepository businessProfileRepository,
            EmailService emailService,
            EncryptionService encryptionService
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.emailService = emailService;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public void requestUserEmailVerification(User user) {

        if (user == null || user.isEmailVerified()) {
            return;
        }

        String recipient = getUserEmail(user);

        if (recipient == null || recipient.isBlank()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        tokenRepository.deleteByExpiresAtBefore(now);
        tokenRepository.deleteByUserAndTargetType(
                user,
                EmailVerificationTargetType.USER_ACCOUNT
        );

        String rawToken = generateRawToken();

        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setTokenHash(hashToken(rawToken));
        token.setTargetType(EmailVerificationTargetType.USER_ACCOUNT);
        token.setUser(user);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusHours(TOKEN_EXPIRATION_HOURS));
        token.setUsed(false);

        tokenRepository.save(token);

        emailService.sendUserEmailVerificationEmail(
                recipient,
                user.getFirstName(),
                emailService.buildEmailVerificationLink(rawToken)
        );
    }

    @Transactional
    public void requestBusinessEmailVerification(BusinessProfile businessProfile) {

        if (businessProfile == null ||
                businessProfile.isEmailVerified() ||
                businessProfile.getEmail() == null ||
                businessProfile.getEmail().isBlank()) {

            return;
        }

        LocalDateTime now = LocalDateTime.now();

        tokenRepository.deleteByExpiresAtBefore(now);
        tokenRepository.deleteByBusinessProfileAndTargetType(
                businessProfile,
                EmailVerificationTargetType.BUSINESS_PROFILE
        );

        String rawToken = generateRawToken();

        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setTokenHash(hashToken(rawToken));
        token.setTargetType(EmailVerificationTargetType.BUSINESS_PROFILE);
        token.setBusinessProfile(businessProfile);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusHours(TOKEN_EXPIRATION_HOURS));
        token.setUsed(false);

        tokenRepository.save(token);

        emailService.sendBusinessEmailVerificationEmail(
                businessProfile.getEmail().trim().toLowerCase(),
                businessProfile.getName(),
                emailService.buildEmailVerificationLink(rawToken)
        );
    }

    @Transactional
    public boolean verify(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        EmailVerificationToken token =
                tokenRepository
                        .findByTokenHashAndUsedFalse(hashToken(rawToken))
                        .orElse(null);

        if (token == null ||
                token.getExpiresAt().isBefore(LocalDateTime.now())) {

            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (token.getTargetType() ==
                EmailVerificationTargetType.USER_ACCOUNT) {

            User user = token.getUser();

            if (user == null) {
                return false;
            }

            user.setEmailVerified(true);
            user.setEmailVerifiedAt(now);
            userRepository.save(user);

        } else if (token.getTargetType() ==
                EmailVerificationTargetType.BUSINESS_PROFILE) {

            BusinessProfile businessProfile =
                    token.getBusinessProfile();

            if (businessProfile == null) {
                return false;
            }

            businessProfile.setEmailVerified(true);
            businessProfile.setEmailVerifiedAt(now);
            businessProfileRepository.save(businessProfile);

        } else {
            return false;
        }

        token.setUsed(true);
        tokenRepository.save(token);

        return true;
    }

    public void applyBusinessEmailVerificationState(
            BusinessProfile businessProfile,
            User owner
    ) {

        if (businessProfile == null) {
            return;
        }

        businessProfile.setEmailVerified(false);
        businessProfile.setEmailVerifiedAt(null);

        if (owner == null ||
                !owner.isEmailVerified() ||
                businessProfile.getEmail() == null ||
                businessProfile.getEmail().isBlank()) {

            return;
        }

        String ownerEmail = getUserEmail(owner);

        if (businessProfile.getEmail()
                .trim()
                .equalsIgnoreCase(ownerEmail)) {

            businessProfile.setEmailVerified(true);
            businessProfile.setEmailVerifiedAt(LocalDateTime.now());
        }
    }

    private String getUserEmail(User user) {

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim().toLowerCase();
        }

        if (user.getEmailEncrypted() != null &&
                !user.getEmailEncrypted().isBlank()) {

            return encryptionService
                    .decrypt(user.getEmailEncrypted())
                    .trim()
                    .toLowerCase();
        }

        return null;
    }

    private String generateRawToken() {

        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponibil", e);
        }
    }
}
