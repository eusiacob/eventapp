package com.example.eventapp.service;

import com.example.eventapp.model.AccountPurpose;
import com.example.eventapp.model.Role;
import com.example.eventapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.mail.promotion-reminders.enabled", havingValue = "true", matchIfMissing = true)
public class PromotionReminderScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionReminderScheduler.class);
    private final UserRepository users;
    private final PromotionReminderService reminders;

    public PromotionReminderScheduler(UserRepository users, PromotionReminderService reminders) {
        this.users = users;
        this.reminders = reminders;
    }

    @Scheduled(fixedDelayString = "${app.mail.promotion-reminders.delay-ms:60000}",
               initialDelayString = "${app.mail.promotion-reminders.initial-delay-ms:60000}")
    public void sendDueReminders() {
        var ids = users.findDuePromotionReminders(LocalDateTime.now(), Role.BUSINESS,
                List.of(AccountPurpose.PROMOTE_SERVICES, AccountPurpose.BOTH), PageRequest.of(0, 10));
        for (Long id : ids) {
            try {
                reminders.deliver(id);
            } catch (RuntimeException exception) {
                LOGGER.warn("Procesarea reamintirii pentru contul {} a eșuat ({}).",
                        id, exception.getClass().getSimpleName());
            }
        }
    }
}
