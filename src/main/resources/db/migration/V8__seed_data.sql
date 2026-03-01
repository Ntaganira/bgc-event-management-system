-- V8__seed_data.sql
-- Seed default permissions
INSERT INTO permissions (name, description) VALUES
('VIEW_DASHBOARD',    'View main dashboard'),
('CREATE_EVENT',      'Create a new event'),
('EDIT_EVENT',        'Edit an existing event'),
('DELETE_EVENT',      'Delete an event'),
('VIEW_EVENT',        'View events'),
('MARK_ATTENDANCE',   'Mark or scan attendance'),
('VIEW_ATTENDANCE',   'View attendance records'),
('MANAGE_USERS',      'Create, edit, delete users'),
('VIEW_USERS',        'View user list'),
('MANAGE_ROLES',      'Create and assign roles'),
('VIEW_AUDIT_LOGS',   'View system audit logs'),
('SEND_NOTIFICATION', 'Send email notifications');

-- Seed default roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN',     'Full system access'),
('ROLE_ORGANIZER', 'Can create and manage events'),
('ROLE_STAFF',     'Can mark attendance'),
('ROLE_USER',      'Basic registered user');

-- Assign ALL permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_ADMIN';

-- ORGANIZER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
  ON p.name IN ('VIEW_DASHBOARD','CREATE_EVENT','EDIT_EVENT','VIEW_EVENT',
                'VIEW_ATTENDANCE','VIEW_USERS','SEND_NOTIFICATION')
WHERE r.name = 'ROLE_ORGANIZER';

-- STAFF permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
  ON p.name IN ('VIEW_DASHBOARD','VIEW_EVENT','MARK_ATTENDANCE','VIEW_ATTENDANCE')
WHERE r.name = 'ROLE_STAFF';

-- USER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
  ON p.name IN ('VIEW_DASHBOARD','VIEW_EVENT')
WHERE r.name = 'ROLE_USER';

-- Default admin user (password: password123 - BCrypt)
INSERT INTO users (first_name, last_name, email, phone_number, branch, title, password, enabled, email_confirmed)
VALUES ('System', 'Admin', 'ntaganira71@gmail.com', '+250780000000', 'HQ', 'Administrator',
        '$2a$10$u29/avQnlNTuD49LrIiizOw8WHmj8JIml8VZpnsLrK6cpJcNzEZtS', TRUE, TRUE);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'ntaganira71@gmail.com' AND r.name = 'ROLE_ADMIN';

-- Sample events
INSERT INTO events (title, description, location, start_date_time, end_date_time, created_by) VALUES
('Annual Staff Conference 2026', 'Yearly gathering of all staff members across branches',
 'Kigali Convention Center', '2026-03-05 08:00:00', '2026-03-05 17:00:00', 1),
('Leadership Training Workshop', 'Advanced leadership skills for team leads',
 'BGC HQ - Conference Room A', '2026-03-10 09:00:00', '2026-03-10 16:00:00', 1),
('Community Outreach Day', 'Community engagement activities in Musanze',
 'Musanze Branch', '2026-03-15 08:00:00', '2026-03-15 15:00:00', 1);
