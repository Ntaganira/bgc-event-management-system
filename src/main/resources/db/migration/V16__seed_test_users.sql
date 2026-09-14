-- ================================================
-- V16 — Test accounts for local testing
-- All test users share the password: password1234
-- (BCrypt hash below)
-- ================================================

INSERT INTO users (first_name, last_name, email, phone_number, title, password, enabled, email_confirmed, office_id, user_code) VALUES
('Alice',  'Mugisha',  'alice@test.bgc.com',  '+250788100001', 'Accountant',          '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'HQ'),  'BGC-2026-TEST1'),
('Bob',    'Uwase',    'bob@test.bgc.com',    '+254710000002', 'Marketing Officer',   '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'NBI'), 'BGC-2026-TEST2'),
('Celine', 'Uwimana',  'celine@test.bgc.com', '+256750000003', 'Front Desk Officer',  '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'KMP'), 'BGC-2026-TEST3'),
('David',  'Nkunda',   'david@test.bgc.com',  '+234810000004', 'Events Coordinator',  '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'LGS'), 'BGC-2026-TEST4'),
('Elise',  'Umutoni',  'elise@test.bgc.com',  '+233300000005', 'Program Assistant',   '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'ACC'), 'BGC-2026-TEST5'),
('Frank',  'Ingabire', 'frank@test.bgc.com',  '+277300000006', 'Operations Officer',   '$2a$10$lSkvKpanI2BcCX93mk6XQOzupS92LZRqyjopzaWfkPJvfRLRTlFA2', TRUE, TRUE, (SELECT id FROM bcc_offices WHERE code = 'JHB'), 'BGC-2026-TEST6');

-- Assign roles: Alice, Bob, Elise -> ROLE_USER ; Celine, Frank -> ROLE_STAFF ; David -> ROLE_ORGANIZER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email IN ('alice@test.bgc.com', 'bob@test.bgc.com', 'elise@test.bgc.com')
  AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email IN ('celine@test.bgc.com', 'frank@test.bgc.com')
  AND r.name = 'ROLE_STAFF';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'david@test.bgc.com'
  AND r.name = 'ROLE_ORGANIZER';