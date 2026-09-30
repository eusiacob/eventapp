package com.example.eventapp.service;

import com.example.eventapp.repository.LegalDocumentEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class LegalDocumentEmailDeliveryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegalDocumentEmailDeliveryService.class);
    private final LegalDocumentEmailRepository repository;
    private final EncryptionService encryptionService;
    private final EmailService emailService;

    public LegalDocumentEmailDeliveryService(LegalDocumentEmailRepository repository,
            EncryptionService encryptionService, EmailService emailService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.emailService = emailService;
    }

    @Transactional
    public void deliver(Long id) {
        var delivery = repository.findForDelivery(id).orElse(null);
        if (delivery == null || delivery.getNextAttemptAt().isAfter(LocalDateTime.now())) {
            return;
        }
        try {
            var user = delivery.getUser();
            String recipient = encryptionService.decrypt(user.getEmailEncrypted());
            emailService.sendLegalDocumentUpdatedEmail(recipient, user.getFirstName(),
                    delivery.getDocumentType(), delivery.getVersion(), delivery.getLastUpdated());
        } catch (RuntimeException exception) {
            int attempts = Math.min(delivery.getAttempts() + 1, 30);
            delivery.setAttempts(attempts);
            delivery.setNextAttemptAt(LocalDateTime.now().plusMinutes(
                    Math.min(1440L, 1L << Math.min(attempts - 1, 11))));
            // Do not log addresses or SMTP messages containing personal data.
            LOGGER.warn("Notificarea legală {} va fi reîncercată; încercare {}, eroare {}.",
                    id, attempts, exception.getClass().getSimpleName());
            return;
        }
        repository.delete(delivery);
        LOGGER.info("Notificarea legală {} a fost acceptată de serverul SMTP.", id);
    }
}
