-- V13: Add optional profile fields to users
ALTER TABLE users ADD COLUMN bio               TEXT;
ALTER TABLE users ADD COLUMN profile_picture   VARCHAR(500);
ALTER TABLE users ADD COLUMN linkedin_url      VARCHAR(500);
ALTER TABLE users ADD COLUMN website_url       VARCHAR(500);
