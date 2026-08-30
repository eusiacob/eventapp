package com.example.eventapp.repository;

import com.example.eventapp.model.SupportMessage;
import com.example.eventapp.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportMessageRepository
        extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findAllByTicketOrderByCreatedAtAsc(
            SupportTicket ticket
    );
}