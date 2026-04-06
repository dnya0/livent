CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    visibility VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    type VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uk_chat_rooms_event_type UNIQUE (event_id, type)
);
