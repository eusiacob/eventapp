package com.example.eventapp.repository;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.EmailVerificationTargetType;
import com.example.eventapp.model.EmailVerificationToken;
import com.example.eventapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHashAndUsedFalse(
            String tokenHash
    );

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUserAndTargetType(
            User user,
            EmailVerificationTargetType targetType
    );

    void deleteByBusinessProfileAndTargetType(
            BusinessProfile businessProfile,
            EmailVerificationTargetType targetType
    );

    @Modifying
    @Query("delete from EmailVerificationToken token where token.businessProfile = :businessProfile")
    int deleteByBusinessProfile(@Param("businessProfile") BusinessProfile businessProfile);

    @Modifying
    @Query("delete from EmailVerificationToken token where token.user = :user")
    int deleteByUser(@Param("user") User user);
}
