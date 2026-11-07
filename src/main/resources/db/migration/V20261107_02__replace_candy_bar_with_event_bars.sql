-- Run after V20261107_01 (if it has not already been applied), on local and production
-- BEFORE deploying the application. Confirm there are no RESTAURANT profiles first.
-- This script preserves existing Candy Bar profiles and their selected service types.

-- Temporarily add the new category before moving the existing profile records.
ALTER TABLE business_profile
    MODIFY COLUMN category ENUM(
        'ALTELE',
        'ARTIFICII',
        'BARURI',
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
        'RESTAURANT',
        'TRANSPORT',
        'VIDEOGRAF'
    ) NULL;

UPDATE business_service_types
SET service_type = CASE service_type
    WHEN 'CANDY_BAR_CANDY_BAR' THEN 'BARURI_CANDY_BAR'
    WHEN 'CANDY_BAR_TORTURI_PERSONALIZATE' THEN 'BARURI_TORTURI_PERSONALIZATE'
    WHEN 'CANDY_BAR_PRAJITURI_SI_DESERTURI' THEN 'BARURI_PRAJITURI_SI_DESERTURI'
    WHEN 'CANDY_BAR_GELATO_INGHETATA' THEN 'BARURI_GELATO_INGHETATA'
    WHEN 'CANDY_BAR_FANTANA_DE_CIOCOLATA' THEN 'BARURI_FANTANA_DE_CIOCOLATA'
    WHEN 'CANDY_BAR_ALT_SERVICIU' THEN 'BARURI_ALT_SERVICIU'
    ELSE service_type
END
WHERE service_type IN (
    'CANDY_BAR_CANDY_BAR',
    'CANDY_BAR_TORTURI_PERSONALIZATE',
    'CANDY_BAR_PRAJITURI_SI_DESERTURI',
    'CANDY_BAR_GELATO_INGHETATA',
    'CANDY_BAR_FANTANA_DE_CIOCOLATA',
    'CANDY_BAR_ALT_SERVICIU'
);

UPDATE business_profile
SET category = 'BARURI'
WHERE category = 'CANDY_BAR';

-- Remove the retired categories after their records have been moved.
ALTER TABLE business_profile
    MODIFY COLUMN category ENUM(
        'ALTELE',
        'ARTIFICII',
        'BARURI',
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
