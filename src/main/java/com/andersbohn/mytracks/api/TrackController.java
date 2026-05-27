package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.Track;
import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  public record TrackSummary(
      UUID id,
      String trackName,
      String source,
      String sourceId,
      String activityType,
      Instant uploadTimestamp,
      String notes) {

    static TrackSummary from(Track t) {
      return new TrackSummary(
          t.getId(),
          t.getTrackName(),
          t.getSource(),
          t.getSourceId(),
          t.getActivityType(),
          t.getUploadTimestamp(),
          t.getNotes());
    }
  }
}
