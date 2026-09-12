-- Rulează această migrare o singură dată înainte de deploy.
-- Aplicația folosește spring.jpa.hibernate.ddl-auto=validate, deci coloanele
-- trebuie să existe înainte de pornire.
ALTER TABLE `user`
    ADD COLUMN privacy_policy_version VARCHAR(32) NULL,
    ADD COLUMN privacy_policy_accepted_at DATETIME NULL,
    ADD COLUMN terms_version VARCHAR(32) NULL,
    ADD COLUMN terms_accepted_at DATETIME NULL;
