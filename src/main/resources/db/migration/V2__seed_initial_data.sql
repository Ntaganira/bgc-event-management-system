-- V2__seed_initial_data.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 22.
-- Description: Seed initial roles, permissions, and admin user

-- Insert roles
INSERT INTO roles (name, description, created_at) VALUES 
('ADMIN', 'System Administrator', CURRENT_TIMESTAMP),
('ORGANIZER', 'Event Organizer', CURRENT_TIMESTAMP),
('PUBLIC', 'Public User', CURRENT_TIMESTAMP);

-- Insert permissions
INSERT INTO permissions (name, description, resource, action, created_at) VALUES 
-- User permissions
('USER_CREATE', 'Create users', 'USER', 'CREATE', CURRENT_TIMESTAMP),
('USER_READ', 'Read users', 'USER', 'READ', CURRENT_TIMESTAMP),
('USER_UPDATE', 'Update users', 'USER', 'UPDATE', CURRENT_TIMESTAMP),
('USER_DELETE', 'Delete users', 'USER', 'DELETE', CURRENT_TIMESTAMP),

-- Event permissions
('EVENT_CREATE', 'Create events', 'EVENT', 'CREATE', CURRENT_TIMESTAMP),
('EVENT_READ', 'Read events', 'EVENT', 'READ', CURRENT_TIMESTAMP),
('EVENT_UPDATE', 'Update events', 'EVENT', 'UPDATE', CURRENT_TIMESTAMP),
('EVENT_DELETE', 'Delete events', 'EVENT', 'DELETE', CURRENT_TIMESTAMP),
('EVENT_PUBLISH', 'Publish events', 'EVENT', 'PUBLISH', CURRENT_TIMESTAMP),
('EVENT_CANCEL', 'Cancel events', 'EVENT', 'CANCEL', CURRENT_TIMESTAMP),

-- Registration permissions
('REGISTRATION_READ', 'Read registrations', 'REGISTRATION', 'READ', CURRENT_TIMESTAMP),
('REGISTRATION_CREATE', 'Create registrations', 'REGISTRATION', 'CREATE', CURRENT_TIMESTAMP),
('REGISTRATION_UPDATE', 'Update registrations', 'REGISTRATION', 'UPDATE', CURRENT_TIMESTAMP),
('REGISTRATION_DELETE', 'Delete registrations', 'REGISTRATION', 'DELETE', CURRENT_TIMESTAMP),
('REGISTRATION_CHECKIN', 'Check-in attendees', 'REGISTRATION', 'CHECKIN', CURRENT_TIMESTAMP),

-- Report permissions
('REPORT_VIEW', 'View reports', 'REPORT', 'VIEW', CURRENT_TIMESTAMP),
('REPORT_EXPORT', 'Export reports', 'REPORT', 'EXPORT', CURRENT_TIMESTAMP),

-- System permissions
('SYSTEM_CONFIG', 'Configure system', 'SYSTEM', 'CONFIG', CURRENT_TIMESTAMP),
('AUDIT_VIEW', 'View audit logs', 'AUDIT', 'VIEW', CURRENT_TIMESTAMP);

-- Assign permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Assign permissions to ORGANIZER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ORGANIZER' 
AND p.name IN (
    'EVENT_CREATE', 'EVENT_READ', 'EVENT_UPDATE', 'EVENT_DELETE',
    'EVENT_PUBLISH', 'EVENT_CANCEL',
    'REGISTRATION_READ', 'REGISTRATION_CHECKIN',
    'REPORT_VIEW', 'REPORT_EXPORT'
);

-- Assign permissions to PUBLIC role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'PUBLIC' 
AND p.name IN ('EVENT_READ', 'REGISTRATION_CREATE');

-- Insert admin user (password: Admin@123 - BCrypt encoded)
INSERT INTO users (
    username, email, password, first_name, last_name, 
    enabled, created_at, created_by
) VALUES (
    'admin',
    'admin@bgc.event',
    '$2a$10$N.zmJ9CFsC7XTZ5QkQxqQO5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5',
    'System',
    'Administrator',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM'
);

-- Insert sample organizer
INSERT INTO users (
    username, email, password, first_name, last_name, 
    enabled, created_at, created_by
) VALUES (
    'organizer1',
    'organizer1@bgc.event',
    '$2a$10$N.zmJ9CFsC7XTZ5QkQxqQO5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5',
    'John',
    'Organizer',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM'
);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'organizer1' AND r.name = 'ORGANIZER';

-- Give PUBLIC role to all users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE r.name = 'PUBLIC'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);