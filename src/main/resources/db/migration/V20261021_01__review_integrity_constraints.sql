-- Prevent duplicate reviews for the same user and service, including concurrent requests.
-- Before applying this migration, check for pre-existing duplicates:
-- SELECT business_id, user_id, COUNT(*) FROM review
-- GROUP BY business_id, user_id HAVING COUNT(*) > 1;
-- Resolve any returned rows without deleting review content, then apply the constraint.
ALTER TABLE review
    ADD CONSTRAINT uk_review_business_user UNIQUE (business_id, user_id);
