CREATE TABLE users (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    display_name   VARCHAR(255),
    sso_provider   VARCHAR(255) NOT NULL,
    sso_subject    VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    UNIQUE (sso_provider, sso_subject)
);

DROP INDEX idx_tracks_user_id;
ALTER TABLE tracks DROP COLUMN user_id;

ALTER TABLE tracks
    ADD COLUMN user_id      UUID        NOT NULL REFERENCES users(id),
    ADD COLUMN activity_type VARCHAR(255),
    ADD COLUMN notes         TEXT;

CREATE INDEX idx_tracks_user_id ON tracks (user_id);
