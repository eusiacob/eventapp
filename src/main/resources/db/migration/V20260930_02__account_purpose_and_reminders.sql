-- Rulează o singură dată înainte de instalarea noului JAR.
-- Conturile existente aleg scopul din Profil; nu primesc reamintiri automat.
ALTER TABLE `user`
    ADD COLUMN account_purpose VARCHAR(32) NOT NULL DEFAULT 'UNSPECIFIED',
    ADD COLUMN promotion_started_at DATETIME(6) NULL,
    ADD COLUMN next_promotion_reminder_at DATETIME(6) NULL,
    ADD COLUMN promotion_reminders_sent INT NOT NULL DEFAULT 0,
    ADD COLUMN promotion_reminder_attempts INT NOT NULL DEFAULT 0;

CREATE INDEX idx_user_promotion_due ON `user` (next_promotion_reminder_at);
