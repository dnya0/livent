CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    visibility VARCHAR(32) NOT NULL,
    CONSTRAINT chk_events_visibility CHECK (visibility IN ('ONSITE', 'ONLINE', 'BOTH')),
    CONSTRAINT chk_events_time_range CHECK (start_time < end_time)
);

CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    type VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT chk_chat_rooms_type CHECK (type IN ('GLOBAL', 'LOCAL', 'SESSION')),
    CONSTRAINT uk_chat_rooms_event_type UNIQUE (event_id, type)
);
