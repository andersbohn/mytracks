package com.andersbohn.mytracks.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class TrackRepositoryIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired UserRepository userRepository;
  @Autowired TrackRepository trackRepository;

  @Test
  void trackRoundTrip() {
    var user =
        userRepository.save(
            new User("carol@example.com", "Carol", "google", "google-sub-3", Instant.now()));
    var track =
        trackRepository.save(
            new Track(
                user, "Morning Run", "garmin", null, Instant.now(), "running", "Felt great", null));

    var loaded = trackRepository.findById(track.getId()).orElseThrow();
    assertThat(loaded.getUser().getId()).isEqualTo(user.getId());
    assertThat(loaded.getActivityType()).isEqualTo("running");
    assertThat(loaded.getNotes()).isEqualTo("Felt great");
    assertThat(loaded.getSourceId()).isNull();
  }

  @Test
  void sourceIdRoundTrip() {
    var user =
        userRepository.save(
            new User("dave@example.com", "Dave", "google", "google-sub-4", Instant.now()));
    var track =
        trackRepository.save(
            new Track(
                user, "Summit Hike", "garmin", "12345678", Instant.now(), "hiking", null, null));

    var loaded = trackRepository.findById(track.getId()).orElseThrow();
    assertThat(loaded.getSourceId()).isEqualTo("12345678");
  }
}
