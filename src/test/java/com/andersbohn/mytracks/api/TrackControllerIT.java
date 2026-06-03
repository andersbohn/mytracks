package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.andersbohn.mytracks.domain.Track;
import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import java.time.Instant;
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
    if (mockUser.getRole() != UserRole.USER) {
      mockUser.setRole(UserRole.USER);
      mockUser = userRepository.save(mockUser);
    }
  }

  @Test
  void noTracks_returnsEmptyPage() {
    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.content()).isEmpty();
    assertThat(body.totalElements()).isEqualTo(0);
  }

  @Test
  void withTracks_returnsSummariesInPage() {
    trackRepository.save(
        new Track(
            mockUser, "Evening Ride", "garmin", "99887766", Instant.now(), "cycling", null, null));

    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.totalElements()).isEqualTo(1);
    assertThat(body.content())
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

    var response = restTemplate.getForEntity("/api/tracks", TrackController.TrackPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().content())
        .anyMatch(t -> "Morning Run".equals(t.trackName()) && t.sourceId() == null);
  }

  @Test
  void paging_returnsCorrectPage() {
    for (int i = 0; i < 25; i++) {
      trackRepository.save(
          new Track(mockUser, "Track " + i, "garmin", null, Instant.now(), null, null, null));
    }

    var response =
        restTemplate.getForEntity("/api/tracks?page=1&size=10", TrackController.TrackPage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.content()).hasSize(10);
    assertThat(body.totalElements()).isEqualTo(25);
    assertThat(body.totalPages()).isEqualTo(3);
    assertThat(body.number()).isEqualTo(1);
  }
}
