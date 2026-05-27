package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.andersbohn.mytracks.domain.Track;
import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TrackControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;
  @Autowired TrackRepository trackRepository;
  @Autowired UserRepository userRepository;

  private User mockUser;

  @BeforeEach
  void setUp() {
    trackRepository.deleteAll();
    mockUser =
        userRepository
            .findByEmail("test@example.com")
            .orElseGet(
                () ->
                    userRepository.save(
                        new User(
                            "test@example.com",
                            "Test User",
                            "mock",
                            "test@example.com",
                            Instant.now())));
  }

  @Test
  void noTracks_returnsEmptyList() {
    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackSummary[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void withTracks_returnsSummaries() {
    trackRepository.save(
        new Track(
            mockUser, "Evening Ride", "garmin", "99887766", Instant.now(), "cycling", null, null));

    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackSummary[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var tracks = response.getBody();
    assertThat(tracks).isNotNull();
    assertThat(Arrays.stream(tracks))
        .anyMatch(
            t ->
                "Evening Ride".equals(t.trackName())
                    && "99887766".equals(t.sourceId())
                    && "cycling".equals(t.activityType()));
  }

  @Test
  void sourceId_nullWhenNotSet() {
    trackRepository.save(
        new Track(mockUser, "Morning Run", "garmin", null, Instant.now(), "running", null, null));

    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackSummary[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(Arrays.stream(response.getBody()))
        .anyMatch(t -> "Morning Run".equals(t.trackName()) && t.sourceId() == null);
  }
}
