package com.example.eventapp.repository;

import com.example.eventapp.model.LegalDocument;
import com.example.eventapp.model.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    Optional<LegalDocument> findByType(LegalDocumentType type);
}
