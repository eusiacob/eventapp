package com.example.eventapp.repository;

import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.model.AccountPurpose;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmailHash(String emailHash);

    boolean existsByEmailHash(String emailHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findForPurposeUpdate(@Param("id") Long id);

    @Query("""
            select u.id from User u where u.enabled = true and u.emailVerified = true
            and u.role = :role and u.accountPurpose in :purposes
            and u.promotionRemindersSent < 3 and u.nextPromotionReminderAt <= :now
            and not exists (select b.id from BusinessProfile b where b.user = u)
            order by u.nextPromotionReminderAt, u.id
            """)
    List<Long> findDuePromotionReminders(@Param("now") LocalDateTime now,
            @Param("role") Role role, @Param("purposes") List<AccountPurpose> purposes, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.favoriteBusinesses b WHERE b.id = :businessId")
    List<User> findUsersWhoFavoriteBusiness(@Param("businessId") Long businessId);

    List<User> findByRole(Role role);

    List<User> findByEnabledTrueAndLastActivityAtBefore(
            LocalDateTime date
    );

    List<User> findByLastActivityAtBefore(LocalDateTime date);
}
