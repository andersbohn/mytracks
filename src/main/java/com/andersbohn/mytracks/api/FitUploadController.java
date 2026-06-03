package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.FitMetadata;
import com.andersbohn.mytracks.domain.FitParser;
import com.andersbohn.mytracks.domain.Track;
import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tracks/fit")
public class FitUploadController {

  private final TrackRepository trackRepository;
  private final UserRepository userRepository;

  public FitUploadController(TrackRepository trackRepository, UserRepository userRepository) {
    this.trackRepository = trackRepository;
    this.userRepository = userRepository;
  }

  @PutMapping(value = "/{activityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TrackController.TrackSummary> uploadFit(
      @AuthenticationPrincipal OAuth2User principal,
      @PathVariable String activityId,
      @RequestParam MultipartFile file)
      throws IOException {
    if (file.isEmpty()) return ResponseEntity.badRequest().build();
    var userOpt = userRepository.findByEmail(principal.getAttribute("email"));
    if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
    var user = userOpt.get();
    if (user.getRole() == UserRole.GUEST) return ResponseEntity.status(403).build();

    byte[] bytes = file.getBytes();
    var meta = FitParser.parse(new ByteArrayInputStream(bytes));

    if (meta.activityId() != null && !meta.activityId().equals(activityId))
      return ResponseEntity.status(409).build();

    return ResponseEntity.ok(
        TrackController.TrackSummary.from(persist(user, activityId, bytes, meta)));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TrackController.TrackSummary> uploadFitAuto(
      @AuthenticationPrincipal OAuth2User principal, @RequestParam MultipartFile file)
      throws IOException {
    if (file.isEmpty()) return ResponseEntity.badRequest().build();
    var userOpt = userRepository.findByEmail(principal.getAttribute("email"));
    if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
    var user = userOpt.get();
    if (user.getRole() == UserRole.GUEST) return ResponseEntity.status(403).build();

    byte[] bytes = file.getBytes();
    var meta = FitParser.parse(new ByteArrayInputStream(bytes));

    String activityId = meta.activityId();
    if (activityId == null) {
      String filename = file.getOriginalFilename();
      if (filename != null && filename.toLowerCase().endsWith(".fit"))
        activityId = filename.substring(0, filename.length() - 4);
    }
    if (activityId == null || activityId.isBlank()) return ResponseEntity.badRequest().build();

    return ResponseEntity.ok(
        TrackController.TrackSummary.from(persist(user, activityId, bytes, meta)));
  }

  private Track persist(User user, String activityId, byte[] bytes, FitMetadata meta) {
    var track =
        trackRepository
            .findByUserAndSourceId(user, activityId)
            .orElseGet(
                () ->
                    new Track(
                        user,
                        activityId,
                        "fit-upload",
                        activityId,
                        Instant.now(),
                        null,
                        null,
                        null));

    track.setRawPayload(bytes);
    if (meta.startTime() != null) track.setStartTime(meta.startTime());
    if (meta.durationSeconds() != null) track.setDurationSeconds(meta.durationSeconds());
    if (meta.movingTimeSeconds() != null) track.setMovingTimeSeconds(meta.movingTimeSeconds());
    if (meta.distanceMeters() != null) track.setDistanceMeters(meta.distanceMeters());
    if (meta.ascentMeters() != null) track.setAscentMeters(meta.ascentMeters());
    if (meta.descentMeters() != null) track.setDescentMeters(meta.descentMeters());
    if (meta.avgHeartRate() != null) track.setAvgHeartRate(meta.avgHeartRate());
    if (meta.maxHeartRate() != null) track.setMaxHeartRate(meta.maxHeartRate());
    if (meta.avgSpeedMs() != null) track.setAvgSpeedMs(meta.avgSpeedMs());
    if (meta.maxSpeedMs() != null) track.setMaxSpeedMs(meta.maxSpeedMs());
    if (meta.calories() != null) track.setCalories(meta.calories());
    if (meta.avgPowerWatts() != null) track.setAvgPowerWatts(meta.avgPowerWatts());
    if (meta.normalizedPowerWatts() != null)
      track.setNormalizedPowerWatts(meta.normalizedPowerWatts());
    if (meta.avgCadence() != null) track.setAvgCadence(meta.avgCadence());
    if (meta.sport() != null) track.setActivityType(meta.sport());
    if (meta.sport() != null) track.setSport(meta.sport());
    if (meta.subSport() != null) track.setSubSport(meta.subSport());

    return trackRepository.save(track);
  }
}
