package com.andersbohn.mytracks.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "track_name", nullable = false)
    private String trackName;

    @Column(nullable = false)
    private String source;

    @Column(name = "upload_timestamp", nullable = false)
    private Instant uploadTimestamp;

    @Column(name = "raw_payload", columnDefinition = "bytea")
    private byte[] rawPayload;

    protected Track() {}

    public Track(String userId, String trackName, String source, Instant uploadTimestamp, byte[] rawPayload) {
        this.userId = userId;
        this.trackName = trackName;
        this.source = source;
        this.uploadTimestamp = uploadTimestamp;
        this.rawPayload = rawPayload;
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getTrackName() { return trackName; }
    public String getSource() { return source; }
    public Instant getUploadTimestamp() { return uploadTimestamp; }
    public byte[] getRawPayload() { return rawPayload; }

    public void setTrackName(String trackName) { this.trackName = trackName; }
    public void setRawPayload(byte[] rawPayload) { this.rawPayload = rawPayload; }
}
