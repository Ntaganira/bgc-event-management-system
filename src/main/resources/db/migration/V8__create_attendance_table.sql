-- V8__create_attendance_table.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 22.
-- Description: Create attendance table for check-in tracking (FR-16 to FR-19)

CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    event_id BIGINT NOT NULL REFERENCES events(id),
    registration_id BIGINT NOT NULL REFERENCES registrations(id),
    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checked_in_by BIGINT REFERENCES users(id),
    check_in_method VARCHAR(20),
    qr_code_used VARCHAR(100),
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    latitude DOUBLE PRECISION,  -- Changed to match Java Double
    longitude DOUBLE PRECISION, -- Changed to match Java Double
    notes TEXT,
    
    CONSTRAINT unique_registration_attendance UNIQUE (registration_id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_attendance_event ON attendances(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_registration ON attendances(registration_id);
CREATE INDEX IF NOT EXISTS idx_attendance_checked_in_at ON attendances(checked_in_at);
CREATE INDEX IF NOT EXISTS idx_attendance_method ON attendances(check_in_method);
CREATE INDEX IF NOT EXISTS idx_attendance_checked_by ON attendances(checked_in_by);

-- Create function to update registration status on attendance
CREATE OR REPLACE FUNCTION update_registration_on_checkin()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE registrations 
    SET checked_in = true, 
        checked_in_at = NEW.checked_in_at,
        status = 'ATTENDED',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.registration_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_registration_on_checkin ON attendances;
CREATE TRIGGER trigger_update_registration_on_checkin
    AFTER INSERT ON attendances
    FOR EACH ROW
    EXECUTE FUNCTION update_registration_on_checkin();

-- Create function to update event attendance count
CREATE OR REPLACE FUNCTION update_event_attendance_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE events 
        SET current_attendees = COALESCE(current_attendees, 0) + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.event_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE events 
        SET current_attendees = GREATEST(COALESCE(current_attendees, 0) - 1, 0),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.event_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_event_attendance ON attendances;
CREATE TRIGGER trigger_update_event_attendance
    AFTER INSERT OR DELETE ON attendances
    FOR EACH ROW
    EXECUTE FUNCTION update_event_attendance_count();