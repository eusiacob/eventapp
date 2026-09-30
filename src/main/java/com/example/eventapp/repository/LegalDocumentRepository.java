package com.example.eventapp.repository;

import com.example.eventapp.model.LegalDocument;
import com.example.eventapp.model.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    Optional<LegalDocument> findByType(LegalDocumentType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from LegalDocument d where d.type = :type")
    Optional<LegalDocument> findByTypeForUpdate(@Param("type") LegalDocumentType type);
}
