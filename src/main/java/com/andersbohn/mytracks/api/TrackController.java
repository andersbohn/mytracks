package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.GpxParser;
import com.andersbohn.mytracks.domain.Track;
import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

  private final TrackRepository trackRepository;
  private final UserRepository userRepository;

  public TrackController(TrackRepository trackRepository, UserRepository userRepository) {
    this.trackRepository = trackRepository;
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<TrackSummary>> list(@AuthenticationPrincipal OAuth2User principal) {
    String email = principal.getAttribute("email");
    return userRepository
        .findByEmail(email)
        .map(
            user ->
                ResponseEntity.ok(
                    trackRepository.findByUser(user).stream().map(TrackSummary::from).toList()))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TrackSummary> upload(
      @AuthenticationPrincipal OAuth2User principal,
      @RequestParam MultipartFile file,
      @RequestParam(required = false) String trackName,
      @RequestParam(required = false) String sourceId,
      @RequestParam(required = false) String activityType,
      @RequestParam(required = false) String notes)
      throws IOException {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    String email = principal.getAttribute("email");
    var userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    var user = userOpt.get();
    if (user.getRole() == UserRole.GUEST) {
      return ResponseEntity.status(403).build();
    }
    byte[] gpxBytes = file.getBytes();
    var meta = GpxParser.parse(gpxBytes);
    String resolvedName =
        firstNonBlank(trackName, meta.name(), stripExtension(file.getOriginalFilename()));
    String resolvedType = firstNonBlank(activityType, meta.type());
    var track =
        trackRepository.save(
            new Track(
                user,
                resolvedName,
                "gpx-upload",
                sourceId,
                Instant.now(),
                resolvedType,
                notes,
                gpxBytes));
    return ResponseEntity.ok(TrackSummary.from(track));
  }

  private static String firstNonBlank(String... candidates) {
    for (String s : candidates) {
      if (s != null && !s.isBlank()) return s.trim();
    }
    return "untitled";
  }

  private static String stripExtension(String filename) {
    if (filename == null) return null;
    int dot = filename.lastIndexOf('.');
    return dot > 0 ? filename.substring(0, dot) : filename;
  }

  public record TrackSummary(
      UUID id,
      String trackName,
      String source,
      String sourceId,
      String activityType,
      Instant uploadTimestamp,
      String notes,
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
      String subSport) {

    static TrackSummary from(Track t) {
      return new TrackSummary(
          t.getId(),
          t.getTrackName(),
          t.getSource(),
          t.getSourceId(),
          t.getActivityType(),
          t.getUploadTimestamp(),
          t.getNotes(),
          t.getStartTime(),
          t.getDurationSeconds(),
          t.getMovingTimeSeconds(),
          t.getDistanceMeters(),
          t.getAscentMeters(),
          t.getDescentMeters(),
          t.getAvgHeartRate(),
          t.getMaxHeartRate(),
          t.getAvgSpeedMs(),
          t.getMaxSpeedMs(),
          t.getCalories(),
          t.getAvgPowerWatts(),
          t.getNormalizedPowerWatts(),
          t.getAvgCadence(),
          t.getSport(),
          t.getSubSport());
    }
  }
}
