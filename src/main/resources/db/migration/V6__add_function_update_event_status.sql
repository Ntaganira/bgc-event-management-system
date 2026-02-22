-- V6__add_function_update_event_status.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 21.
-- Description: Add function to automatically update event status

-- Create function to update event status based on conditions
CREATE OR REPLACE FUNCTION update_event_status()
RETURNS TRIGGER AS $$
DECLARE
    now_timestamp TIMESTAMP := CURRENT_TIMESTAMP;
BEGIN
    -- Don't update cancelled or completed events
    IF NEW.status IN ('CANCELLED', 'COMPLETED') THEN
        RETURN NEW;
    END IF;
    
    -- Check if event is completed
    IF NEW.end_date < now_timestamp THEN
        NEW.status := 'COMPLETED';
    -- Check if event is full
    ELSIF NEW.capacity IS NOT NULL AND NEW.current_registrations >= NEW.capacity THEN
        NEW.status := 'FULL';
    -- Check if registration deadline passed
    ELSIF NEW.registration_deadline IS NOT NULL AND NEW.registration_deadline < now_timestamp THEN
        NEW.status := 'CLOSED';
    -- Check if event is open
    ELSIF NEW.status = 'DRAFT' AND NEW.published_at IS NOT NULL THEN
        NEW.status := 'OPEN';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for events
CREATE TRIGGER update_event_status_trigger
    BEFORE UPDATE ON events
    FOR EACH ROW
    WHEN (OLD.current_registrations IS DISTINCT FROM NEW.current_registrations
          OR OLD.end_date IS DISTINCT FROM NEW.end_date
          OR OLD.registration_deadline IS DISTINCT FROM NEW.registration_deadline)
    EXECUTE FUNCTION update_event_status();

-- Create scheduled function to update all events periodically
CREATE OR REPLACE FUNCTION refresh_all_event_statuses()
RETURNS void AS $$
BEGIN
    UPDATE events 
    SET updated_at = CURRENT_TIMESTAMP
    WHERE status NOT IN ('CANCELLED', 'COMPLETED');
END;
$$ LANGUAGE plpgsql;