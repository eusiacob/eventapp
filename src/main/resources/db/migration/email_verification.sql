ALTER TABLE `user`
    ADD COLUMN email_verified bit NOT NULL DEFAULT 0,
    ADD COLUMN email_verified_at datetime(6) NULL;

ALTER TABLE business_profile
    ADD COLUMN email_verified bit NOT NULL DEFAULT 0,
    ADD COLUMN email_verified_at datetime(6) NULL;

CREATE TABLE email_verification_token (
    id bigint NOT NULL AUTO_INCREMENT,
    token_hash varchar(64) NOT NULL,
    target_type varchar(32) NOT NULL,
    user_id bigint NULL,
    business_profile_id bigint NULL,
    expires_at datetime(6) NOT NULL,
    used bit NOT NULL DEFAULT 0,
    created_at datetime(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verification_token_hash (token_hash),
    KEY idx_email_verification_user (user_id),
    KEY idx_email_verification_business (business_profile_id),
    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_email_verification_business
        FOREIGN KEY (business_profile_id) REFERENCES business_profile (id) ON DELETE CASCADE
);
