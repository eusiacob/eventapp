-- Run once on the local database and production BEFORE starting the new application.
-- No business_profile rows should use RESTAURANT when this migration is applied.
ALTER TABLE business_profile
    MODIFY COLUMN category ENUM(
        'ALTELE',
        'ARTIFICII',
        'CANDY_BAR',
        'CATERING',
        'DECOR',
        'DIVERTISMENT',
        'DJ',
        'FLORIST',
        'FOTOGRAF',
        'LOCATII',
      'MUZICA_LIVE',
      'ORGANIZATOR',
        'TRANSPORT',
        'VIDEOGRAF'
    ) NULL;
