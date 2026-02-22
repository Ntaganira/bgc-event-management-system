-- V3__create_sample_events.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 21.
-- Description: Create sample events for testing

-- Insert sample events
DO $$
DECLARE
    organizer_id BIGINT;
    next_month TIMESTAMP;
    two_months TIMESTAMP;
BEGIN
    -- Get organizer ID
    SELECT id INTO organizer_id FROM users WHERE username = 'organizer1';
    
    -- Set dates
    next_month := CURRENT_TIMESTAMP + INTERVAL '1 month';
    two_months := CURRENT_TIMESTAMP + INTERVAL '2 months';
    
    -- Sample Event 1: Tech Conference
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        created_at, created_by
    ) VALUES (
        'BGC Tech Conference 2026',
        'Join us for the biggest tech conference of the year! Featuring keynotes from industry leaders, hands-on workshops, and networking opportunities.',
        'Annual tech conference with workshops and networking',
        'BGC Convention Center',
        '123 Business Ave',
        'Taguig City',
        next_month + INTERVAL '2 days 9 hours',
        next_month + INTERVAL '2 days 18 hours',
        next_month,
        500,
        'OPEN',
        '#4287f5',
        true,
        false,
        organizer_id,
        CURRENT_TIMESTAMP,
        'SYSTEM'
    );
    
    -- Sample Event 2: Workshop
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        created_at, created_by
    ) VALUES (
        'Spring Boot Masterclass',
        'Intensive 2-day workshop on building production-ready applications with Spring Boot. Learn best practices, testing, and deployment.',
        'Hands-on Spring Boot workshop',
        'BGC Innovation Hub',
        '456 Tech Park',
        'Taguig City',
        next_month + INTERVAL '10 days 9 hours',
        next_month + INTERVAL '11 days 17 hours',
        next_month + INTERVAL '8 days',
        50,
        'OPEN',
        '#42f5b0',
        true,
        true,
        organizer_id,
        CURRENT_TIMESTAMP,
        'SYSTEM'
    );
    
    -- Sample Event 3: Networking Event
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        created_at, created_by
    ) VALUES (
        'Tech Networking Night',
        'Evening networking event for tech professionals. Connect with peers, recruiters, and industry experts over drinks and appetizers.',
        'Networking mixer for tech professionals',
        'Sky Lounge',
        '789 Tower',
        'Taguig City',
        two_months + INTERVAL '5 days 18 hours',
        two_months + INTERVAL '5 days 22 hours',
        two_months + INTERVAL '3 days',
        200,
        'OPEN',
        '#f54242',
        false,
        false,
        organizer_id,
        CURRENT_TIMESTAMP,
        'SYSTEM'
    );
    
    -- Add tags for events
    INSERT INTO event_tags (event_id, tag)
    SELECT e.id, t.tag
    FROM events e
    CROSS JOIN (VALUES ('technology'), ('conference'), ('workshop')) AS t(tag)
    WHERE e.title = 'BGC Tech Conference 2026';
    
    INSERT INTO event_tags (event_id, tag)
    SELECT e.id, t.tag
    FROM events e
    CROSS JOIN (VALUES ('java'), ('spring'), ('workshop'), ('backend')) AS t(tag)
    WHERE e.title = 'Spring Boot Masterclass';
    
    INSERT INTO event_tags (event_id, tag)
    SELECT e.id, t.tag
    FROM events e
    CROSS JOIN (VALUES ('networking'), ('social'), ('tech')) AS t(tag)
    WHERE e.title = 'Tech Networking Night';
    
    -- Add to V3__create_sample_events.sql after existing inserts

    -- Add a FULL event
    INSERT INTO events (
        title, description, venue, start_date, end_date, 
        capacity, current_registrations, status, organizer_id, created_at
    ) VALUES (
        'Sold Out Workshop',
        'This event is completely full',
        'BGC Innovation Hub',
        CURRENT_TIMESTAMP + INTERVAL '15 days',
        CURRENT_TIMESTAMP + INTERVAL '15 days 4 hours',
        50, 50, 'FULL',
        (SELECT id FROM users WHERE username = 'organizer1'),
        CURRENT_TIMESTAMP
    );

    -- Add a COMPLETED event
    INSERT INTO events (
        title, description, venue, start_date, end_date, 
        capacity, current_registrations, status, organizer_id, created_at
    ) VALUES (
        'Past Event',
        'This event already happened',
        'BGC Convention Center',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days 5 hours',
        100, 85, 'COMPLETED',
        (SELECT id FROM users WHERE username = 'organizer1'),
        CURRENT_TIMESTAMP - INTERVAL '20 days'
    );

    -- Add a CANCELLED event
    INSERT INTO events (
        title, description, venue, start_date, end_date, 
        capacity, status, organizer_id, created_at
    ) VALUES (
        'Cancelled Conference',
        'This event was cancelled',
        'BGC Events Place',
        CURRENT_TIMESTAMP + INTERVAL '5 days',
        CURRENT_TIMESTAMP + INTERVAL '5 days 8 hours',
        200, 'CANCELLED',
        (SELECT id FROM users WHERE username = 'organizer1'),
        CURRENT_TIMESTAMP - INTERVAL '5 days'
    );
END $$;