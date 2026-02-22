-- V9__add_sample_audit_logs.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 22.
-- Description: Add sample audit logs for testing

DO $$
DECLARE
    admin_id BIGINT;
    organizer_id BIGINT;
    tech_conf_id BIGINT;
    workshop_id BIGINT;
BEGIN
    -- Get user IDs
    SELECT id INTO admin_id FROM users WHERE username = 'admin';
    SELECT id INTO organizer_id FROM users WHERE username = 'organizer1';
    
    -- Get event IDs
    SELECT id INTO tech_conf_id FROM events WHERE title = 'BGC Tech Conference 2026';
    SELECT id INTO workshop_id FROM events WHERE title = 'Spring Boot Masterclass';
    
    -- User authentication logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, user_email, entity_type, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '1 hour', 'LOGIN_SUCCESS', 'LOGIN', admin_id, 'admin', 'admin@bgc.event', 'AUTH', '192.168.1.100', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '2 hours', 'LOGIN_SUCCESS', 'LOGIN', organizer_id, 'organizer1', 'organizer1@bgc.event', 'AUTH', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '3 hours', 'LOGIN_FAILURE', 'LOGIN', NULL, NULL, 'unknown@example.com', 'AUTH', '192.168.1.200', 'FAILURE'),
    (CURRENT_TIMESTAMP - INTERVAL '1 day', 'LOGOUT', 'LOGOUT', admin_id, 'admin', 'admin@bgc.event', 'AUTH', '192.168.1.100', 'SUCCESS');
    
    -- Event management logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, entity_type, entity_id, entity_name, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '3 hours', 'CREATE_EVENT', 'CREATE', organizer_id, 'organizer1', 'EVENT', tech_conf_id, 'BGC Tech Conference 2026', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '2 hours', 'UPDATE_EVENT', 'UPDATE', organizer_id, 'organizer1', 'EVENT', tech_conf_id, 'BGC Tech Conference 2026', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '1 hour', 'PUBLISH_EVENT', 'UPDATE', organizer_id, 'organizer1', 'EVENT', tech_conf_id, 'BGC Tech Conference 2026', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '2 days', 'CREATE_EVENT', 'CREATE', organizer_id, 'organizer1', 'EVENT', workshop_id, 'Spring Boot Masterclass', '192.168.1.101', 'SUCCESS');
    
    -- Registration logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, user_email, entity_type, entity_id, entity_name, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '1 day', 'REGISTER', 'CREATE', NULL, NULL, 'john.doe@example.com', 'REGISTRATION', 1, 'John Doe', '192.168.1.150', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '23 hours', 'CONFIRM_REGISTRATION', 'UPDATE', NULL, NULL, 'john.doe@example.com', 'REGISTRATION', 1, 'John Doe', '192.168.1.150', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '12 hours', 'CANCEL_REGISTRATION', 'DELETE', NULL, NULL, 'jane.smith@example.com', 'REGISTRATION', 5, 'Jane Smith', '192.168.1.151', 'SUCCESS');
    
    -- Check-in logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, entity_type, entity_id, entity_name, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '6 hours', 'CHECK_IN', 'CHECKIN', organizer_id, 'organizer1', 'ATTENDANCE', 1, 'John Doe', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '5 hours', 'BULK_CHECK_IN', 'CHECKIN', organizer_id, 'organizer1', 'ATTENDANCE', NULL, 'Bulk check-in 25 attendees', '192.168.1.101', 'SUCCESS');
    
    -- Export logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, entity_type, entity_id, entity_name, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '4 hours', 'EXPORT_REGISTRATIONS', 'EXPORT', organizer_id, 'organizer1', 'REPORT', tech_conf_id, 'Tech Conference Registrations', '192.168.1.101', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '3 hours', 'EXPORT_EVENTS', 'EXPORT', admin_id, 'admin', 'REPORT', NULL, 'All Events Summary', '192.168.1.100', 'SUCCESS');
    
    -- User management logs
    INSERT INTO audit_logs (created_at, action, action_category, user_id, username, entity_type, entity_id, entity_name, ip_address, status) VALUES
    (CURRENT_TIMESTAMP - INTERVAL '1 day', 'CREATE_USER', 'CREATE', admin_id, 'admin', 'USER', 3, 'New User', '192.168.1.100', 'SUCCESS'),
    (CURRENT_TIMESTAMP - INTERVAL '12 hours', 'UPDATE_USER', 'UPDATE', admin_id, 'admin', 'USER', 2, 'Updated User', '192.168.1.100', 'SUCCESS');
    
END $$;