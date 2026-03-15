-- V15: Add unique QR identifier to each user
ALTER TABLE users ADD COLUMN user_code VARCHAR(255);
UPDATE users SET user_code = CONCAT('BGC-', YEAR(created_at), '-', CAST(id AS VARCHAR)) WHERE user_code IS NULL;