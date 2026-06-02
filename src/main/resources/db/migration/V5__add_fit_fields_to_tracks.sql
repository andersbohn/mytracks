ALTER TABLE tracks
    ADD COLUMN start_time             TIMESTAMPTZ,
    ADD COLUMN duration_seconds       INTEGER,
    ADD COLUMN moving_time_seconds    INTEGER,
    ADD COLUMN distance_meters        NUMERIC(10, 2),
    ADD COLUMN ascent_meters          NUMERIC(8, 2),
    ADD COLUMN descent_meters         NUMERIC(8, 2),
    ADD COLUMN avg_heart_rate         INTEGER,
    ADD COLUMN max_heart_rate         INTEGER,
    ADD COLUMN avg_speed_ms           NUMERIC(7, 4),
    ADD COLUMN max_speed_ms           NUMERIC(7, 4),
    ADD COLUMN calories               INTEGER,
    ADD COLUMN avg_power_watts        INTEGER,
    ADD COLUMN normalized_power_watts INTEGER,
    ADD COLUMN avg_cadence            INTEGER,
    ADD COLUMN sport                  VARCHAR(64),
    ADD COLUMN sub_sport              VARCHAR(64);

CREATE UNIQUE INDEX uq_tracks_user_source ON tracks (user_id, source_id) WHERE source_id IS NOT NULL;
