-- Rulează această migrare o singură dată după V20260912_01.
CREATE TABLE legal_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    version VARCHAR(32) NOT NULL,
    last_updated DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_legal_documents_type UNIQUE (document_type)
);
