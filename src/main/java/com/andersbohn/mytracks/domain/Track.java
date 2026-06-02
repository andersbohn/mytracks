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

  @Column(name = "start_time")
  private Instant startTime;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "moving_time_seconds")
  private Integer movingTimeSeconds;

  @Column(name = "distance_meters")
  private Double distanceMeters;

  @Column(name = "ascent_meters")
  private Double ascentMeters;

  @Column(name = "descent_meters")
  private Double descentMeters;

  @Column(name = "avg_heart_rate")
  private Integer avgHeartRate;

  @Column(name = "max_heart_rate")
  private Integer maxHeartRate;

  @Column(name = "avg_speed_ms")
  private Double avgSpeedMs;

  @Column(name = "max_speed_ms")
  private Double maxSpeedMs;

  @Column(name = "calories")
  private Integer calories;

  @Column(name = "avg_power_watts")
  private Integer avgPowerWatts;

  @Column(name = "normalized_power_watts")
  private Integer normalizedPowerWatts;

  @Column(name = "avg_cadence")
  private Integer avgCadence;

  @Column(name = "sport")
  private String sport;

  @Column(name = "sub_sport")
  private String subSport;

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

  public Instant getStartTime() {
    return startTime;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public Integer getMovingTimeSeconds() {
    return movingTimeSeconds;
  }

  public Double getDistanceMeters() {
    return distanceMeters;
  }

  public Double getAscentMeters() {
    return ascentMeters;
  }

  public Double getDescentMeters() {
    return descentMeters;
  }

  public Integer getAvgHeartRate() {
    return avgHeartRate;
  }

  public Integer getMaxHeartRate() {
    return maxHeartRate;
  }

  public Double getAvgSpeedMs() {
    return avgSpeedMs;
  }

  public Double getMaxSpeedMs() {
    return maxSpeedMs;
  }

  public Integer getCalories() {
    return calories;
  }

  public Integer getAvgPowerWatts() {
    return avgPowerWatts;
  }

  public Integer getNormalizedPowerWatts() {
    return normalizedPowerWatts;
  }

  public Integer getAvgCadence() {
    return avgCadence;
  }

  public String getSport() {
    return sport;
  }

  public String getSubSport() {
    return subSport;
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

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public void setDurationSeconds(Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public void setMovingTimeSeconds(Integer movingTimeSeconds) {
    this.movingTimeSeconds = movingTimeSeconds;
  }

  public void setDistanceMeters(Double distanceMeters) {
    this.distanceMeters = distanceMeters;
  }

  public void setAscentMeters(Double ascentMeters) {
    this.ascentMeters = ascentMeters;
  }

  public void setDescentMeters(Double descentMeters) {
    this.descentMeters = descentMeters;
  }

  public void setAvgHeartRate(Integer avgHeartRate) {
    this.avgHeartRate = avgHeartRate;
  }

  public void setMaxHeartRate(Integer maxHeartRate) {
    this.maxHeartRate = maxHeartRate;
  }

  public void setAvgSpeedMs(Double avgSpeedMs) {
    this.avgSpeedMs = avgSpeedMs;
  }

  public void setMaxSpeedMs(Double maxSpeedMs) {
    this.maxSpeedMs = maxSpeedMs;
  }

  public void setCalories(Integer calories) {
    this.calories = calories;
  }

  public void setAvgPowerWatts(Integer avgPowerWatts) {
    this.avgPowerWatts = avgPowerWatts;
  }

  public void setNormalizedPowerWatts(Integer normalizedPowerWatts) {
    this.normalizedPowerWatts = normalizedPowerWatts;
  }

  public void setAvgCadence(Integer avgCadence) {
    this.avgCadence = avgCadence;
  }

  public void setSport(String sport) {
    this.sport = sport;
  }

  public void setSubSport(String subSport) {
    this.subSport = subSport;
  }
}
