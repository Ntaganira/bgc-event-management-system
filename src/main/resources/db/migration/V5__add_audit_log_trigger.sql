-- V5__add_audit_log_trigger.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 21.
-- Description: Add triggers for automatic audit logging

-- Create function to log user actions
CREATE OR REPLACE FUNCTION log_entity_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_logs (
            action, user_id, username, entity_type, entity_id, 
            details, created_at
        ) VALUES (
            'CREATE',
            NEW.created_by::bigint,
            NEW.created_by,
            TG_TABLE_NAME,
            NEW.id,
            jsonb_build_object('data', row_to_json(NEW)),
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_logs (
            action, user_id, username, entity_type, entity_id, 
            details, created_at
        ) VALUES (
            'UPDATE',
            NEW.updated_by::bigint,
            NEW.updated_by,
            TG_TABLE_NAME,
            NEW.id,
            jsonb_build_object(
                'old', row_to_json(OLD),
                'new', row_to_json(NEW)
            ),
            CURRENT_TIMESTAMP
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_logs (
            action, user_id, username, entity_type, entity_id, 
            details, created_at
        ) VALUES (
            'DELETE',
            OLD.deleted_by::bigint,
            OLD.deleted_by,
            TG_TABLE_NAME,
            OLD.id,
            jsonb_build_object('data', row_to_json(OLD)),
            CURRENT_TIMESTAMP
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for each table
CREATE TRIGGER audit_users_trigger
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION log_entity_changes();

CREATE TRIGGER audit_events_trigger
    AFTER INSERT OR UPDATE OR DELETE ON events
    FOR EACH ROW EXECUTE FUNCTION log_entity_changes();

CREATE TRIGGER audit_registrations_trigger
    AFTER INSERT OR UPDATE OR DELETE ON registrations
    FOR EACH ROW EXECUTE FUNCTION log_entity_changes();