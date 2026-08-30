package com.example.eventapp.service;

import com.example.eventapp.model.SupportMessage;
import com.example.eventapp.model.SupportTicket;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.SupportMessageRepository;
import com.example.eventapp.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserNotificationService userNotificationService;

    public SupportService(
            SupportTicketRepository supportTicketRepository,
            SupportMessageRepository supportMessageRepository,
            UserNotificationService userNotificationService
    ) {
        this.supportTicketRepository = supportTicketRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.userNotificationService = userNotificationService;
    }


    // =========================
    // TICKETE
    // =========================

    public SupportTicket createTicket(
            User user,
            String subject,
            SupportTicket.SupportCategory category,
            String message
    ) {

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Subiectul nu poate fi gol."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Selectează o categorie."
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Mesajul nu poate fi gol."
            );
        }

        SupportTicket ticket = new SupportTicket();

        ticket.setUser(user);
        ticket.setSubject(subject.trim());
        ticket.setCategory(category);
        ticket.setStatus(
                SupportTicket.SupportStatus.OPEN
        );

        SupportTicket savedTicket =
                supportTicketRepository.save(ticket);

        // Primul mesaj al conversației

        SupportMessage supportMessage =
                new SupportMessage();

        supportMessage.setTicket(savedTicket);
        supportMessage.setUser(user);
        supportMessage.setMessage(message.trim());

        supportMessageRepository.save(
                supportMessage
        );

        userNotificationService.notifyAdminsNewSupportTicket(savedTicket);

        return savedTicket;
    }


    public List<SupportTicket> getUserTickets(
            User user
    ) {

        return supportTicketRepository
                .findAllByUserOrderByUpdatedAtDesc(user);
    }


    public SupportTicket getTicket(
            Long ticketId
    ) {

        return supportTicketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Solicitarea nu există."
                        )
                );
    }


    // =========================
    // VERIFICARE PERMISIUNI
    // =========================

    public SupportTicket getUserTicket(
            Long ticketId,
            User user
    ) {

        SupportTicket ticket =
                getTicket(ticketId);

        if (ticket.getUser() == null ||
                !ticket.getUser().getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "Nu ai permisiunea să accesezi această solicitare."
            );
        }

        return ticket;
    }


    // =========================
    // MESAJE
    // =========================

    public List<SupportMessage> getMessages(
            SupportTicket ticket
    ) {

        return supportMessageRepository
                .findAllByTicketOrderByCreatedAtAsc(
                        ticket
                );
    }


    @Transactional
    public SupportMessage addMessage(
            Long ticketId,
            User user,
            String message
    ) {

        if (message == null ||
                message.isBlank()) {

            throw new IllegalArgumentException(
                    "Mesajul nu poate fi gol."
            );
        }

        SupportTicket ticket =
                getUserTicket(
                        ticketId,
                        user
                );


        if (ticket.getStatus() ==
                SupportTicket.SupportStatus.CLOSED) {

            throw new IllegalStateException(
                    "Această solicitare este închisă."
            );
        }


        SupportMessage supportMessage =
                new SupportMessage();

        supportMessage.setTicket(ticket);
        supportMessage.setUser(user);
        supportMessage.setMessage(
                message.trim()
        );


        SupportMessage savedMessage = supportMessageRepository.save(supportMessage);

        ticket.setStatus(
                SupportTicket.SupportStatus.IN_PROGRESS
        );

        supportTicketRepository.save(ticket);

        // Notificare către administratori

        userNotificationService.notifyAdminsNewSupportMessage(ticket);


        return savedMessage;
    }

    // =========================
    // ADMIN
    // =========================

    public List<SupportTicket> getAllTickets() {

        return supportTicketRepository
                .findAllByOrderByUpdatedAtDesc();
    }


    public List<SupportTicket> getTicketsByStatus(
            SupportTicket.SupportStatus status
    ) {

        return supportTicketRepository
                .findAllByStatusOrderByUpdatedAtDesc(
                        status
                );
    }


    public SupportTicket getAdminTicket(
            Long ticketId
    ) {

        return supportTicketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Solicitarea nu există."
                        )
                );
    }


    @Transactional
    public void addAdminMessage(
            Long ticketId,
            User admin,
            String message
    ) {

        if (message == null ||
                message.isBlank()) {

            throw new IllegalArgumentException(
                    "Mesajul nu poate fi gol."
            );
        }

        SupportTicket ticket =
                getAdminTicket(ticketId);


        if (ticket.getStatus() ==
                SupportTicket.SupportStatus.CLOSED) {

            throw new IllegalStateException(
                    "Această solicitare este închisă."
            );
        }


        SupportMessage supportMessage =
                new SupportMessage();

        supportMessage.setTicket(ticket);
        supportMessage.setUser(admin);
        supportMessage.setMessage(
                message.trim()
        );

        SupportMessage savedMessage = supportMessageRepository.save(supportMessage);

        ticket.setStatus(SupportTicket.SupportStatus.IN_PROGRESS);

        supportTicketRepository.save(ticket);

        // Notificare către utilizator

        userNotificationService.create(
                ticket.getUser(),
                "Răspuns nou de la Support",
                "Ai primit un răspuns nou pentru solicitarea \"" +
                        ticket.getSubject() +
                        "\".",
                "/support/" + ticket.getId()
        );
    }


    @Transactional
    public void updateTicketStatus(
            Long ticketId,
            SupportTicket.SupportStatus status
    ) {

        SupportTicket ticket =
                getAdminTicket(ticketId);

        SupportTicket.SupportStatus oldStatus =
                ticket.getStatus();

        // Nu trimitem notificare dacă statusul nu s-a schimbat
        if (oldStatus == status) {
            return;
        }

        ticket.setStatus(status);

        supportTicketRepository.save(ticket);


        String statusMessage;

        switch (status) {

            case OPEN:
                statusMessage =
                        "Solicitarea ta a fost redeschisă.";
                break;

            case IN_PROGRESS:
                statusMessage =
                        "Solicitarea ta este acum în lucru.";
                break;

            case CLOSED:
                statusMessage =
                        "Solicitarea ta a fost închisă.";
                break;

            default:
                statusMessage =
                        "Statusul solicitării tale a fost modificat.";
        }


        userNotificationService.create(
                ticket.getUser(),
                "Status Support actualizat",
                statusMessage,
                "/support/" + ticket.getId()
        );
    }
}