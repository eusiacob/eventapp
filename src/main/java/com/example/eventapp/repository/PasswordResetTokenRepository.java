package com.example.eventapp.repository;

import com.example.eventapp.model.PasswordResetToken;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(
            String tokenHash
    );

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUser(User user);
}