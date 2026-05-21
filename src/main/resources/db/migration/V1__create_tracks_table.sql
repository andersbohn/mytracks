CREATE TABLE tracks (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         VARCHAR(255) NOT NULL,
    track_name      VARCHAR(255) NOT NULL,
    source          VARCHAR(255) NOT NULL,
    upload_timestamp TIMESTAMPTZ  NOT NULL,
    raw_payload     BYTEA
);

CREATE INDEX idx_tracks_user_id ON tracks (user_id);
