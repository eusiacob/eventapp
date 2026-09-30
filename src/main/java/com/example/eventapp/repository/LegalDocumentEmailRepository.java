package com.example.eventapp.repository;

import com.example.eventapp.model.LegalDocumentEmail;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LegalDocumentEmailRepository extends JpaRepository<LegalDocumentEmail, Long> {
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO legal_document_emails
                (user_id, document_type, version, last_updated, created_at, next_attempt_at, attempts)
            SELECT u.id, :type, :version, :updated, :now, :now, 0 FROM `user` u
            """, nativeQuery = true)
    int enqueueForAllUsers(@Param("type") String type, @Param("version") String version,
                          @Param("updated") LocalDate updated, @Param("now") LocalDateTime now);

    @Query("select e.id from LegalDocumentEmail e where e.nextAttemptAt <= :now order by e.nextAttemptAt, e.id")
    List<Long> findDueIds(@Param("now") LocalDateTime now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LegalDocumentEmail e where e.id = :id")
    Optional<LegalDocumentEmail> findForDelivery(@Param("id") Long id);
}
