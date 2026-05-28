package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TrackUploadIT {

  private static final String MINIMAL_GPX =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <gpx version="1.1" xmlns="http://www.topografix.com/GPX/1/1">
        <trk>
          <name>Morning Hike</name>
          <type>hiking</type>
          <trkseg><trkpt lat="47.0" lon="8.0"/></trkseg>
        </trk>
      </gpx>
      """;

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;
  @Autowired TrackRepository trackRepository;
  @Autowired UserRepository userRepository;

  @BeforeEach
  void setUp() {
    trackRepository.deleteAll();
    var user =
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
    if (user.getRole() != UserRole.USER) {
      user.setRole(UserRole.USER);
      userRepository.save(user);
    }
  }

  @Test
  void upload_extractsNameAndTypeFromGpx() {
    var response = postGpx(MINIMAL_GPX, null, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.trackName()).isEqualTo("Morning Hike");
    assertThat(body.activityType()).isEqualTo("hiking");
    assertThat(body.source()).isEqualTo("gpx-upload");
  }

  @Test
  void upload_trackNameOverrideWinsOverGpx() {
    var response = postGpx(MINIMAL_GPX, "My Override Name", null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().trackName()).isEqualTo("My Override Name");
  }

  @Test
  void upload_sourceIdStored() {
    var response = postGpx(MINIMAL_GPX, null, "12345678");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().sourceId()).isEqualTo("12345678");
  }

  @Test
  void upload_guestUser_returns403() {
    var user = userRepository.findByEmail("test@example.com").orElseThrow();
    user.setRole(UserRole.GUEST);
    userRepository.save(user);

    var response = postGpx(MINIMAL_GPX, null, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void upload_emptyFile_returns400() {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add(
        "file",
        new ByteArrayResource(new byte[0]) {
          @Override
          public String getFilename() {
            return "empty.gpx";
          }
        });
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    var response =
        restTemplate.postForEntity(
            "/api/tracks/upload", new HttpEntity<>(body, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private org.springframework.http.ResponseEntity<TrackController.TrackSummary> postGpx(
      String gpxContent, String trackName, String sourceId) {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add(
        "file",
        new ByteArrayResource(gpxContent.getBytes()) {
          @Override
          public String getFilename() {
            return "track.gpx";
          }
        });
    if (trackName != null) body.add("trackName", trackName);
    if (sourceId != null) body.add("sourceId", sourceId);
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return restTemplate.postForEntity(
        "/api/tracks/upload", new HttpEntity<>(body, headers), TrackController.TrackSummary.class);
  }
}
