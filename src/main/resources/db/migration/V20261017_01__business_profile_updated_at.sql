-- Track the last profile change so public service sitemap entries can expose lastmod.
ALTER TABLE business_profile ADD COLUMN updated_at DATETIME(6) NULL;
UPDATE business_profile
SET updated_at = CAST(created_at AS DATETIME)
WHERE updated_at IS NULL AND created_at IS NOT NULL;
