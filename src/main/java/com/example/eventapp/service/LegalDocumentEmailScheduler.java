package com.example.eventapp.service;

import com.example.eventapp.repository.LegalDocumentEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.mail.legal-notifications.enabled", havingValue = "true", matchIfMissing = true)
public class LegalDocumentEmailScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegalDocumentEmailScheduler.class);
    private final LegalDocumentEmailRepository repository;
    private final LegalDocumentEmailDeliveryService deliveryService;

    public LegalDocumentEmailScheduler(LegalDocumentEmailRepository repository,
                                      LegalDocumentEmailDeliveryService deliveryService) {
        this.repository = repository;
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedDelayString = "${app.mail.legal-notifications.delay-ms:5000}",
               initialDelayString = "${app.mail.legal-notifications.initial-delay-ms:30000}")
    public void deliverPending() {
        for (Long id : repository.findDueIds(LocalDateTime.now(), PageRequest.of(0, 10))) {
            try {
                deliveryService.deliver(id);
            } catch (RuntimeException exception) {
                LOGGER.warn("Coada de notificări legale: procesarea {} a eșuat ({}).",
                        id, exception.getClass().getSimpleName());
            }
        }
    }
}
