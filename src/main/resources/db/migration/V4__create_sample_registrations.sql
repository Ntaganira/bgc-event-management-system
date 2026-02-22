-- V4__create_sample_registrations.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 21.
-- Description: Create sample registrations for testing

-- Insert sample registrations
DO $$
DECLARE
    tech_conf_id BIGINT;
    workshop_id BIGINT;
    networking_id BIGINT;
    i INTEGER;
    qr_code_val VARCHAR(100);
BEGIN
    -- Get event IDs
    SELECT id INTO tech_conf_id FROM events WHERE title = 'BGC Tech Conference 2026';
    SELECT id INTO workshop_id FROM events WHERE title = 'Spring Boot Masterclass';
    SELECT id INTO networking_id FROM events WHERE title = 'Tech Networking Night';
    
    -- Generate QR codes and registrations for Tech Conference
    FOR i IN 1..50 LOOP
        qr_code_val := 'QR' || gen_random_uuid()::text;
        
        INSERT INTO registrations (
            first_name, last_name, email, phone_number, organization,
            job_title, qr_code, status, checked_in, checked_in_at,
            registration_token, token_expiry, event_id, created_at
        ) VALUES (
            'FirstName' || i,
            'LastName' || i,
            'user' || i || '@example.com',
            '+639' || LPAD(i::text, 9, '0'),
            'Company ' || (i % 10 + 1),
            CASE (i % 5)
                WHEN 0 THEN 'Software Engineer'
                WHEN 1 THEN 'Product Manager'
                WHEN 2 THEN 'Data Scientist'
                WHEN 3 THEN 'DevOps Engineer'
                ELSE 'Tech Lead'
            END,
            qr_code_val,
            CASE 
                WHEN i <= 10 THEN 'ATTENDED'
                WHEN i <= 35 THEN 'CONFIRMED'
                WHEN i <= 45 THEN 'PENDING'
                ELSE 'CANCELLED'
            END,
            i <= 10, -- checked_in for first 10
            CASE WHEN i <= 10 THEN CURRENT_TIMESTAMP - INTERVAL '1 hour' ELSE NULL END,
            gen_random_uuid()::text,
            CURRENT_TIMESTAMP + INTERVAL '7 days',
            tech_conf_id,
            CURRENT_TIMESTAMP - (i * INTERVAL '1 hour')
        );
    END LOOP;
    
    -- Update event registration count
    UPDATE events SET 
        current_registrations = 45,
        current_waitlist = 5
    WHERE id = tech_conf_id;
    
    -- Sample registrations for Workshop
    FOR i IN 1..30 LOOP
        qr_code_val := 'QR' || gen_random_uuid()::text;
        
        INSERT INTO registrations (
            first_name, last_name, email, phone_number, organization,
            job_title, qr_code, status, checked_in,
            registration_token, token_expiry, event_id, created_at
        ) VALUES (
            'WorkshopUser' || i,
            'Attendee' || i,
            'workshop' || i || '@example.com',
            '+639' || LPAD((i+50)::text, 9, '0'),
            'Tech Corp ' || (i % 5 + 1),
            CASE (i % 4)
                WHEN 0 THEN 'Java Developer'
                WHEN 1 THEN 'Backend Engineer'
                WHEN 2 THEN 'Full Stack Dev'
                ELSE 'Architect'
            END,
            qr_code_val,
            CASE 
                WHEN i <= 15 THEN 'CONFIRMED'
                WHEN i <= 25 THEN 'PENDING'
                ELSE 'CANCELLED'
            END,
            i <= 5, -- checked_in for first 5
            gen_random_uuid()::text,
            CURRENT_TIMESTAMP + INTERVAL '7 days',
            workshop_id,
            CURRENT_TIMESTAMP - (i * INTERVAL '2 hour')
        );
    END LOOP;
    
    -- Update workshop registration count
    UPDATE events SET 
        current_registrations = 25,
        current_waitlist = 0
    WHERE id = workshop_id;
    
    -- Sample registrations for Networking
    FOR i IN 1..40 LOOP
        qr_code_val := 'QR' || gen_random_uuid()::text;
        
        INSERT INTO registrations (
            first_name, last_name, email, phone_number, organization,
            job_title, qr_code, status, checked_in,
            registration_token, token_expiry, event_id, created_at
        ) VALUES (
            'NetworkingUser' || i,
            'Participant' || i,
            'networking' || i || '@example.com',
            '+639' || LPAD((i+100)::text, 9, '0'),
            'Startup ' || (i % 8 + 1),
            CASE (i % 6)
                WHEN 0 THEN 'CEO'
                WHEN 1 THEN 'CTO'
                WHEN 2 THEN 'Developer'
                WHEN 3 THEN 'Designer'
                WHEN 4 THEN 'Marketing'
                ELSE 'Founder'
            END,
            qr_code_val,
            CASE 
                WHEN i <= 25 THEN 'CONFIRMED'
                WHEN i <= 35 THEN 'PENDING'
                ELSE 'CANCELLED'
            END,
            false, -- none checked in yet (future event)
            gen_random_uuid()::text,
            CURRENT_TIMESTAMP + INTERVAL '7 days',
            networking_id,
            CURRENT_TIMESTAMP - (i * INTERVAL '3 hour')
        );
    END LOOP;
    
    -- Update networking registration count
    UPDATE events SET 
        current_registrations = 35,
        current_waitlist = 0
    WHERE id = networking_id;
    
END $$;