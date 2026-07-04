CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    content VARCHAR(1000) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_messages_type CHECK (type IN ('TEXT', 'SYSTEM', 'NOTICE')),
    CONSTRAINT chk_messages_content_not_blank CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_room_id_id ON messages(chat_room_id, id);
