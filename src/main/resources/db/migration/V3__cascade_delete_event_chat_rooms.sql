ALTER TABLE chat_rooms DROP CONSTRAINT IF EXISTS chat_rooms_event_id_fkey;

ALTER TABLE chat_rooms
    ADD CONSTRAINT chat_rooms_event_id_fkey
        FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE;
