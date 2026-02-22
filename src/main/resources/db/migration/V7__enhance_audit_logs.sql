-- V7__enhance_audit_logs.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 21.
-- Description: Enhance audit logs table with additional fields and indexes

-- Drop existing audit_logs table if we need to recreate (or ALTER)
CREATE TABLE IF NOT EXISTS audit_logs_new (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Action details
    action VARCHAR(50) NOT NULL,
    action_category VARCHAR(30), -- CREATE, UPDATE, DELETE, READ, EXPORT, LOGIN, LOGOUT
    
    -- User details
    user_id BIGINT REFERENCES users(id),
    username VARCHAR(50),
    user_email VARCHAR(100),
    user_role VARCHAR(50),
    
    -- Entity details
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_name VARCHAR(200), -- Human-readable name (event title, user name, etc.)
    
    -- Request details
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(10),
    request_path VARCHAR(255),
    
    -- Changes (JSON)
    old_values JSONB,
    new_values JSONB,
    changes_summary TEXT,
    
    -- Status
    status VARCHAR(20) DEFAULT 'SUCCESS', -- SUCCESS, FAILURE, UNAUTHORIZED
    error_message TEXT,
    
    -- Performance
    execution_time_ms INTEGER
);

-- Migrate existing data if any
INSERT INTO audit_logs_new (
    id, created_at, action, user_id, username, entity_type, entity_id, 
    ip_address, user_agent
)
SELECT 
    id, created_at, action, user_id, username, entity_type, entity_id,
    ip_address, user_agent
FROM audit_logs;

-- Drop old table and rename new one
DROP TABLE IF EXISTS audit_logs CASCADE;
ALTER TABLE audit_logs_new RENAME TO audit_logs;

-- Create indexes (without IF NOT EXISTS - use DROP IF EXISTS first)
DROP INDEX IF EXISTS idx_audit_logs_created;
DROP INDEX IF EXISTS idx_audit_logs_user;
DROP INDEX IF EXISTS idx_audit_logs_entity;
DROP INDEX IF EXISTS idx_audit_logs_action;
DROP INDEX IF EXISTS idx_audit_logs_category;

-- Create indexes for performance
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_category ON audit_logs(action_category);

-- Create function to extract changes summary
CREATE OR REPLACE FUNCTION extract_changes_summary(old_json JSONB, new_json JSONB)
RETURNS TEXT AS $$
DECLARE
    changed_fields TEXT[];
    key TEXT;
    old_val TEXT;
    new_val TEXT;
BEGIN
    IF old_json IS NULL OR new_json IS NULL THEN
        RETURN NULL;
    END IF;
    
    FOR key IN SELECT jsonb_object_keys(old_json) 
                UNION 
                SELECT jsonb_object_keys(new_json)
    LOOP
        old_val := old_json->>key;
        new_val := new_json->>key;
        
        IF old_val IS DISTINCT FROM new_val THEN
            changed_fields := array_append(changed_fields, 
                format('%s: "%s" → "%s"', key, old_val, new_val));
        END IF;
    END LOOP;
    
    IF array_length(changed_fields, 1) > 0 THEN
        RETURN array_to_string(changed_fields, ', ');
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;