-- ================================================
-- V9 — Seed permission descriptions
-- These are already inserted in V8 but without descriptions.
-- Update them so the UI can display readable labels.
-- ================================================

UPDATE permissions SET description = 'Access the main dashboard and statistics'          WHERE name = 'VIEW_DASHBOARD';
UPDATE permissions SET description = 'Create new events'                                  WHERE name = 'CREATE_EVENT';
UPDATE permissions SET description = 'Edit existing events'                               WHERE name = 'EDIT_EVENT';
UPDATE permissions SET description = 'Permanently delete events'                          WHERE name = 'DELETE_EVENT';
UPDATE permissions SET description = 'View event list and details'                        WHERE name = 'VIEW_EVENT';
UPDATE permissions SET description = 'Mark attendance via QR or manual check-in'         WHERE name = 'MARK_ATTENDANCE';
UPDATE permissions SET description = 'View attendance records for all events'             WHERE name = 'VIEW_ATTENDANCE';
UPDATE permissions SET description = 'Enable, disable, and delete user accounts'         WHERE name = 'MANAGE_USERS';
UPDATE permissions SET description = 'View the user list'                                 WHERE name = 'VIEW_USERS';
UPDATE permissions SET description = 'Create, edit, and delete roles and permissions'    WHERE name = 'MANAGE_ROLES';
UPDATE permissions SET description = 'View the full system audit log'                    WHERE name = 'VIEW_AUDIT_LOGS';
UPDATE permissions SET description = 'Send notifications to users or groups'             WHERE name = 'SEND_NOTIFICATION';
