package com.example.eventapp.service;

import com.example.eventapp.model.Role;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PromotionReminderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionReminderService.class);
    private final UserRepository users;
    private final BusinessProfileRepository businesses;
    private final EncryptionService encryption;
    private final EmailService emails;

    public PromotionReminderService(UserRepository users, BusinessProfileRepository businesses,
            EncryptionService encryption, EmailService emails) {
        this.users = users;
        this.businesses = businesses;
        this.encryption = encryption;
        this.emails = emails;
    }

    @Transactional
    public void deliver(Long userId) {
        var user = users.findForPurposeUpdate(userId).orElse(null);
        var now = LocalDateTime.now();
        if (user == null || user.getNextPromotionReminderAt() == null
                || user.getNextPromotionReminderAt().isAfter(now)) {
            return;
        }
        if (!user.isEnabled() || !user.isEmailVerified() || user.getRole() != Role.BUSINESS) {
            return;
        }
        if (user.getAccountPurpose() == null || !user.getAccountPurpose().includesPromotion()
                || user.getPromotionRemindersSent() >= 3 || businesses.existsByUser(user)) {
            user.setNextPromotionReminderAt(null);
            return;
        }
        try {
            emails.sendPromotionReminderEmail(encryption.decrypt(user.getEmailEncrypted()),
                    user.getFirstName(), user.getPromotionRemindersSent());
        } catch (RuntimeException exception) {
            int attempts = Math.min(30, user.getPromotionReminderAttempts() + 1);
            user.setPromotionReminderAttempts(attempts);
            user.setNextPromotionReminderAt(now.plusMinutes(
                    Math.min(1440L, 1L << Math.min(attempts - 1, 11))));
            LOGGER.warn("Reamintirea pentru contul {} va fi reîncercată ({}).",
                    userId, exception.getClass().getSimpleName());
            return;
        }
        int sent = user.getPromotionRemindersSent() + 1;
        user.setPromotionRemindersSent(sent);
        user.setPromotionReminderAttempts(0);
        // Keep messages apart even if SMTP recovered after a prolonged outage.
        user.setNextPromotionReminderAt(sent >= 3 ? null : now.plusDays(sent == 1 ? 7 : 20));
        LOGGER.info("Reamintirea {} pentru contul {} a fost acceptată de SMTP.", sent, userId);
    }
}
