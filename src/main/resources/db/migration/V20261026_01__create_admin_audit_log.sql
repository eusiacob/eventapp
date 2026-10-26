CREATE TABLE admin_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(80) NULL,
    summary VARCHAR(300) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_admin_audit_target_user_created (target_user_id, created_at),
    INDEX idx_admin_audit_created (created_at)
);
