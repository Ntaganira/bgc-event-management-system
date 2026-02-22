-- V3__create_sample_events.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 22.
-- Description: Create sample events for testing

-- Insert sample events
DO $$
DECLARE
    organizer_id BIGINT;
    next_month TIMESTAMP;
    two_months TIMESTAMP;
    past_date TIMESTAMP;
BEGIN
    -- Get organizer ID
    SELECT id INTO organizer_id FROM users WHERE username = 'organizer1';
    
    -- Set dates
    next_month := CURRENT_TIMESTAMP + INTERVAL '1 month';
    two_months := CURRENT_TIMESTAMP + INTERVAL '2 months';
    past_date := CURRENT_TIMESTAMP - INTERVAL '10 days';
    
    -- Sample Event 1: Tech Conference (OPEN)
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        current_registrations, published_at,
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
        45,
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        'SYSTEM'
    );
    
    -- Sample Event 2: Workshop (OPEN)
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        current_registrations, published_at,
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
        25,
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        'SYSTEM'
    );
    
    -- Sample Event 3: Networking Event (OPEN)
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        current_registrations, published_at,
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
        35,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        'SYSTEM'
    );
    
    -- Sample Event 4: FULL event
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        current_registrations, published_at,
        created_at, created_by
    ) VALUES (
        'Sold Out Workshop',
        'This event is completely full',
        'Popular workshop at full capacity',
        'BGC Innovation Hub',
        '456 Tech Park',
        'Taguig City',
        next_month + INTERVAL '15 days 9 hours',
        next_month + INTERVAL '15 days 17 hours',
        next_month + INTERVAL '13 days',
        50,
        'FULL',
        '#dc3545',
        true,
        false,
        organizer_id,
        50,
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        'SYSTEM'
    );
    
    -- Sample Event 5: COMPLETED event
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, capacity, current_registrations, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        published_at, created_at, created_by
    ) VALUES (
        'Past Tech Summit',
        'This event already happened',
        'Q1 Tech Summit',
        'BGC Convention Center',
        '123 Business Ave',
        'Taguig City',
        past_date,
        past_date + INTERVAL '8 hours',
        200,
        185,
        'COMPLETED',
        '#6c757d',
        false,
        false,
        organizer_id,
        past_date - INTERVAL '10 days',
        past_date - INTERVAL '20 days',
        'SYSTEM'
    );
    
    -- Sample Event 6: CANCELLED event
    INSERT INTO events (
        title, description, short_description, venue, address, city,
        start_date, end_date, registration_deadline, capacity, status,
        color_code, allow_waitlist, require_approval, organizer_id,
        created_at, created_by
    ) VALUES (
        'Cancelled Conference',
        'This event was cancelled',
        'Event cancelled due to unforeseen circumstances',
        'BGC Events Place',
        '321 Conference Ave',
        'Taguig City',
        next_month + INTERVAL '20 days 9 hours',
        next_month + INTERVAL '20 days 18 hours',
        next_month + INTERVAL '15 days',
        300,
        'CANCELLED',
        '#343a40',
        false,
        false,
        organizer_id,
        CURRENT_TIMESTAMP - INTERVAL '15 days',
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
    
END $$;