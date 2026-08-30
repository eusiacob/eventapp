package com.example.eventapp.repository;

import com.example.eventapp.model.SupportTicket;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findAllByUserOrderByUpdatedAtDesc(
            User user
    );

    List<SupportTicket> findAllByOrderByUpdatedAtDesc();

    List<SupportTicket> findAllByStatusOrderByUpdatedAtDesc(
            SupportTicket.SupportStatus status
    );
}