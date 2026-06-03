package com.andersbohn.mytracks.domain;

import java.time.Instant;

public record FitMetadata(
    Instant startTime,
    Integer durationSeconds,
    Integer movingTimeSeconds,
    Double distanceMeters,
    Double ascentMeters,
    Double descentMeters,
    Integer avgHeartRate,
    Integer maxHeartRate,
    Double avgSpeedMs,
    Double maxSpeedMs,
    Integer calories,
    Integer avgPowerWatts,
    Integer normalizedPowerWatts,
    Integer avgCadence,
    String sport,
    String subSport) {}
