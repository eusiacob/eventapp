-- Run once on the local database and production BEFORE starting the new application.
-- This project validates the schema; these scripts are not automatically executed.
ALTER TABLE business_profile
    ADD COLUMN other_service_details varchar(150) NULL;

CREATE TABLE business_service_types (
    business_profile_id bigint NOT NULL,
    service_type varchar(100) NOT NULL,
    PRIMARY KEY (business_profile_id, service_type),
    KEY idx_service_type_business (service_type, business_profile_id),
    CONSTRAINT fk_service_types_business FOREIGN KEY (business_profile_id)
        REFERENCES business_profile (id) ON DELETE CASCADE
);

CREATE TABLE business_event_types (
    business_profile_id bigint NOT NULL,
    event_type varchar(50) NOT NULL,
    PRIMARY KEY (business_profile_id, event_type),
    KEY idx_event_type_business (event_type, business_profile_id),
    CONSTRAINT fk_event_types_business FOREIGN KEY (business_profile_id)
        REFERENCES business_profile (id) ON DELETE CASCADE
);
