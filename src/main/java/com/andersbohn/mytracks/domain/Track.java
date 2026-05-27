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

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "track_name", nullable = false)
  private String trackName;

  @Column(nullable = false)
  private String source;

  @Column(name = "upload_timestamp", nullable = false)
  private Instant uploadTimestamp;

  @Column(name = "activity_type")
  private String activityType;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "source_id")
  private String sourceId;

  @Column(name = "raw_payload", columnDefinition = "bytea")
  private byte[] rawPayload;

  protected Track() {}

  public Track(
      User user,
      String trackName,
      String source,
      String sourceId,
      Instant uploadTimestamp,
      String activityType,
      String notes,
      byte[] rawPayload) {
    this.user = user;
    this.trackName = trackName;
    this.source = source;
    this.sourceId = sourceId;
    this.uploadTimestamp = uploadTimestamp;
    this.activityType = activityType;
    this.notes = notes;
    this.rawPayload = rawPayload;
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getTrackName() {
    return trackName;
  }

  public String getSource() {
    return source;
  }

  public String getSourceId() {
    return sourceId;
  }

  public Instant getUploadTimestamp() {
    return uploadTimestamp;
  }

  public String getActivityType() {
    return activityType;
  }

  public String getNotes() {
    return notes;
  }

  public byte[] getRawPayload() {
    return rawPayload;
  }

  public void setTrackName(String trackName) {
    this.trackName = trackName;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public void setRawPayload(byte[] rawPayload) {
    this.rawPayload = rawPayload;
  }
}
