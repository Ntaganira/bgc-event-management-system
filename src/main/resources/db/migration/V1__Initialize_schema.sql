-- V1__initial_schema.sql
-- Project    : BGC EVENT
-- Date       : 2026. 02. 22.
-- Description: Initial database schema creation

-- Create base tables
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    resource VARCHAR(50),
    action VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    title VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    venue VARCHAR(500) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    registration_deadline TIMESTAMP,
    capacity INTEGER,
    current_registrations INTEGER DEFAULT 0,
    waitlist_capacity INTEGER DEFAULT 0,
    current_waitlist INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    featured_image VARCHAR(500),
    color_code VARCHAR(7) DEFAULT '#3788d8',
    terms_and_conditions TEXT,
    allow_waitlist BOOLEAN DEFAULT FALSE,
    require_approval BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP,
    organizer_id BIGINT NOT NULL REFERENCES users(id),
    
    -- Note: available_spots and is_full are NOT stored in DB
    -- They are calculated at runtime using @Transient
    current_attendees INTEGER DEFAULT 0  -- Added for attendance tracking
);

CREATE TABLE IF NOT EXISTS event_tags (
    event_id BIGINT NOT NULL REFERENCES events(id),
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (event_id, tag)
);

CREATE TABLE IF NOT EXISTS registrations (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    organization VARCHAR(200),
    job_title VARCHAR(100),
    qr_code VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    checked_in BOOLEAN DEFAULT FALSE,
    checked_in_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    special_requirements TEXT,
    dietary_restrictions VARCHAR(500),
    registration_token VARCHAR(100),
    token_expiry TIMESTAMP,
    metadata JSONB,
    event_id BIGINT NOT NULL REFERENCES events(id),
    
    CONSTRAINT unique_event_email UNIQUE (event_id, email)
);

CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    event_id BIGINT NOT NULL REFERENCES events(id),
    registration_id BIGINT NOT NULL REFERENCES registrations(id),
    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checked_in_by BIGINT REFERENCES users(id),
    check_in_method VARCHAR(20),
    qr_code_used VARCHAR(100),
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    latitude DOUBLE PRECISION,  -- Changed from DECIMAL to DOUBLE PRECISION
    longitude DOUBLE PRECISION, -- Changed from DECIMAL to DOUBLE PRECISION
    notes TEXT,
    
    CONSTRAINT unique_registration_attendance UNIQUE (registration_id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    action VARCHAR(50) NOT NULL,
    action_category VARCHAR(30),
    user_id BIGINT REFERENCES users(id),
    username VARCHAR(50),
    user_email VARCHAR(100),
    user_role VARCHAR(50),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_name VARCHAR(200),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(10),
    request_path VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    changes_summary TEXT,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    execution_time_ms INTEGER
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_deleted ON users(deleted);

CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_organizer ON events(organizer_id);
CREATE INDEX idx_events_deleted ON events(deleted);

CREATE INDEX idx_registrations_email ON registrations(email);
CREATE INDEX idx_registrations_qr ON registrations(qr_code);
CREATE INDEX idx_registrations_status ON registrations(status);
CREATE INDEX idx_registrations_event ON registrations(event_id);
CREATE INDEX idx_registrations_checked_in ON registrations(checked_in);

CREATE INDEX idx_attendances_event ON attendances(event_id);
CREATE INDEX idx_attendances_registration ON attendances(registration_id);
CREATE INDEX idx_attendances_checked_in_at ON attendances(checked_in_at);

CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_category ON audit_logs(action_category);