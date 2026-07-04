CREATE TABLE IF NOT EXISTS participations (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_participations_status CHECK (status IN ('ONSITE', 'ONLINE')),
    CONSTRAINT uk_participations_event_user UNIQUE (event_id, user_id)
);
