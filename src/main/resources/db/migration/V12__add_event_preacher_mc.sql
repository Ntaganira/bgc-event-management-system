-- V12: Add preacher and MC fields to events table
ALTER TABLE events ADD COLUMN preacher VARCHAR(255);
ALTER TABLE events ADD COLUMN mc       VARCHAR(255);
