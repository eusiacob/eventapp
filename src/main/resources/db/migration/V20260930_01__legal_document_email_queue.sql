-- Rulează înainte de deploy. Proiectul nu rulează automat migrările SQL (fără Flyway).
CREATE TABLE legal_document_emails (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(32) NOT NULL,
    version VARCHAR(32) NOT NULL,
    last_updated DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    next_attempt_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_legal_email_due (next_attempt_at, id),
    CONSTRAINT fk_legal_email_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
);
