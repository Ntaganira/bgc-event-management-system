-- ================================================
-- V17: Demo data for the BCC Global Congress 2026
-- Requires V16 test users (alice/bob/celine/david/elise/frank @test.bgc.com)
-- ================================================

-- Give test users a proper registration window + congress travel dates
UPDATE users SET
        created_at  = CASE email
            WHEN 'alice@test.bgc.com'  THEN TIMESTAMP '2026-05-12 09:15:00'
            WHEN 'bob@test.bgc.com'    THEN TIMESTAMP '2026-05-12 11:40:00'
            WHEN 'celine@test.bgc.com' THEN TIMESTAMP '2026-06-03 14:05:00'
            WHEN 'david@test.bgc.com'  THEN TIMESTAMP '2026-06-21 10:30:00'
            WHEN 'elise@test.bgc.com'  THEN TIMESTAMP '2026-07-08 16:20:00'
            WHEN 'frank@test.bgc.com'  THEN TIMESTAMP '2026-07-30 09:50:00'
        END,
        arrival_date = '2026-08-24',
        return_date  = '2026-08-27'
WHERE email IN ('alice@test.bgc.com','bob@test.bgc.com','celine@test.bgc.com',
                'david@test.bgc.com','elise@test.bgc.com','frank@test.bgc.com');

-- Congress session events (created by the admin user)
INSERT INTO events (title, description, location, start_date_time, end_date_time, qr_code_value, created_by)
SELECT e.title, e.description, e.location, e.start_dt, e.end_dt, e.qr, u.id
FROM (
    VALUES
    ('Opening Ceremony & Worship Night',
     'Official opening of the BCC Global Congress 2026 with worship and keynote.',
     'Kigali Arena',
     TIMESTAMP '2026-08-24 17:00:00', TIMESTAMP '2026-08-24 19:30:00', 'BGC-EVT-OPEN-2026'),
    ('Plenary: Vision & Leadership',
     'Plenary session on vision, servant leadership and mobilising the next generation.',
     'REC Masoro - Main Hall',
     TIMESTAMP '2026-08-25 09:00:00', TIMESTAMP '2026-08-25 12:00:00', 'BGC-EVT-PLEN1-2026'),
    ('Workshop: Digital Transformation',
     'Hands-on workshop on digital tools for church and community administration.',
     'REC Masoro - Room B',
     TIMESTAMP '2026-08-25 14:00:00', TIMESTAMP '2026-08-25 17:00:00', 'BGC-EVT-WS-DT-2026'),
    ('Plenary: Networking & Collaboration',
     'Branches share best practices and explore cross-border collaboration.',
     'REC Masoro - Main Hall',
     TIMESTAMP '2026-08-26 10:00:00', TIMESTAMP '2026-08-26 13:00:00', 'BGC-EVT-PLEN2-2026'),
    ('Closing Gala Dinner & Awards',
     'Closing celebration with awards and the official handover.',
     'Kigali Convention Centre',
     TIMESTAMP '2026-08-27 18:00:00', TIMESTAMP '2026-08-27 22:00:00', 'BGC-EVT-GALA-2026')
) AS e(title, description, location, start_dt, end_dt, qr)
CROSS JOIN users u
WHERE u.email = 'ntaganira71@gmail.com';

-- Event participants with roles (speakers, MCs, worship)
INSERT INTO event_participants (event_id, user_id, role, note)
SELECT ev.id, us.id, p.role, p.note
FROM (
    VALUES
    ('Opening Ceremony & Worship Night', 'david@test.bgc.com', 'MC',      'Main MC of the evening'),
    ('Opening Ceremony & Worship Night', 'frank@test.bgc.com', 'WORSHIP', 'Worship leader'),
    ('Plenary: Vision & Leadership',     'celine@test.bgc.com', 'SPEAKER', 'Panel speaker'),
    ('Plenary: Vision & Leadership',     'bob@test.bgc.com',   'SPEAKER', 'Panel speaker'),
    ('Workshop: Digital Transformation', 'elise@test.bgc.com', 'SPEAKER', 'Facilitates the workshop'),
    ('Closing Gala Dinner & Awards',     'alice@test.bgc.com', 'MC',      'Awards ceremony MC')
) AS p(title, email, role, note)
JOIN events ev ON ev.title = p.title
JOIN users us ON us.email = p.email;

-- Attendees registered for each congress session
INSERT INTO event_attendees (event_id, user_id)
SELECT ev.id, us.id
FROM events ev, users us
WHERE ev.title IN ('Opening Ceremony & Worship Night',
                   'Plenary: Vision & Leadership',
                   'Closing Gala Dinner & Awards')
  AND us.email IN ('alice@test.bgc.com','bob@test.bgc.com','celine@test.bgc.com',
                   'david@test.bgc.com','elise@test.bgc.com','frank@test.bgc.com');

-- Extra attendance for the workshop session
INSERT INTO event_attendees (event_id, user_id)
SELECT ev.id, us.id
FROM events ev, users us
WHERE ev.title = 'Workshop: Digital Transformation'
  AND us.email IN ('alice@test.bgc.com','celine@test.bgc.com','elise@test.bgc.com');

-- Attendance check-ins (scanned on the day)
INSERT INTO attendance (user_id, event_id, method, attendance_time)
SELECT us.id, ev.id, p.method, p.scanned_at
FROM (
    VALUES
    ('alice@test.bgc.com',  'Opening Ceremony & Worship Night', 'QR',   TIMESTAMP '2026-08-24 17:05:00'),
    ('bob@test.bgc.com',    'Opening Ceremony & Worship Night', 'QR',   TIMESTAMP '2026-08-24 17:08:00'),
    ('celine@test.bgc.com', 'Opening Ceremony & Worship Night', 'QR',   TIMESTAMP '2026-08-24 17:12:00'),
    ('david@test.bgc.com',  'Opening Ceremony & Worship Night', 'CODE', TIMESTAMP '2026-08-24 17:15:00'),
    ('elise@test.bgc.com',  'Plenary: Vision & Leadership',     'QR',   TIMESTAMP '2026-08-25 09:03:00'),
    ('frank@test.bgc.com',  'Plenary: Vision & Leadership',     'CODE', TIMESTAMP '2026-08-25 09:10:00'),
    ('alice@test.bgc.com',  'Workshop: Digital Transformation', 'QR',   TIMESTAMP '2026-08-25 14:04:00'),
    ('celine@test.bgc.com', 'Closing Gala Dinner & Awards',     'QR',   TIMESTAMP '2026-08-27 18:02:00'),
    ('david@test.bgc.com',  'Closing Gala Dinner & Awards',     'CODE', TIMESTAMP '2026-08-27 18:06:00')
) AS p(email, title, method, scanned_at)
JOIN users us ON us.email = p.email
JOIN events ev ON ev.title = p.title;