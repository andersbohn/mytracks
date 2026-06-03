package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.andersbohn.mytracks.domain.TrackRepository;
import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import com.garmin.fit.BufferEncoder;
import com.garmin.fit.DateTime;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.Sport;
import com.garmin.fit.SubSport;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class FitUploadControllerIT {

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

  // PUT tests

  @Test
  void put_parsesAndPersistsFitMetadata() throws Exception {
    var response = putFit(buildFit(12345), "12345");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.sourceId()).isEqualTo("12345");
    assertThat(body.source()).isEqualTo("fit-upload");
    assertThat(body.sport()).isEqualTo("CYCLING");
    assertThat(body.distanceMeters()).isEqualTo(15000.0);
    assertThat(body.avgHeartRate()).isEqualTo(145);
    assertThat(body.calories()).isEqualTo(500);
    assertThat(body.startTime()).isNotNull();
  }

  @Test
  void put_noEmbeddedId_usesPathParam() throws Exception {
    var response = putFit(buildFit(null), "42");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().sourceId()).isEqualTo("42");
  }

  @Test
  void put_embeddedIdMismatch_returns409() throws Exception {
    var response = putFit(buildFit(12345), "99999");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void put_sameActivityId_upserts() throws Exception {
    putFit(buildFit(42), "42");
    assertThat(trackRepository.findAll()).hasSize(1);

    var response = putFit(buildFit(42), "42");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(trackRepository.findAll()).hasSize(1);
  }

  @Test
  void put_guestUser_returns403() throws Exception {
    var user = userRepository.findByEmail("test@example.com").orElseThrow();
    user.setRole(UserRole.GUEST);
    userRepository.save(user);

    assertThat(putFit(buildFit(null), "99").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void put_emptyFile_returns400() {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add("file", emptyFitResource("empty.fit"));
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    var response =
        restTemplate.exchange(
            "/api/tracks/fit/123", HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // POST tests

  @Test
  void post_embeddedId_usesEmbeddedId() throws Exception {
    var response = postFit(buildFit(12345), "ignored.fit");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().sourceId()).isEqualTo("12345");
  }

  @Test
  void post_noEmbeddedId_usesFilename() throws Exception {
    var response = postFit(buildFit(null), "17305.fit");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().sourceId()).isEqualTo("17305");
  }

  @Test
  void post_noEmbeddedIdNonFitFilename_returns400() throws Exception {
    var response = postFit(buildFit(null), "activity.gpx");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void post_sameActivityId_upserts() throws Exception {
    postFit(buildFit(null), "555.fit");
    assertThat(trackRepository.findAll()).hasSize(1);

    var response = postFit(buildFit(null), "555.fit");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(trackRepository.findAll()).hasSize(1);
  }

  @Test
  void post_guestUser_returns403() throws Exception {
    var user = userRepository.findByEmail("test@example.com").orElseThrow();
    user.setRole(UserRole.GUEST);
    userRepository.save(user);

    assertThat(postFit(buildFit(null), "1.fit").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void post_emptyFile_returns400() {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add("file", emptyFitResource("empty.fit"));
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    var response =
        restTemplate.exchange(
            "/api/tracks/fit", HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // Helpers

  private org.springframework.http.ResponseEntity<TrackController.TrackSummary> putFit(
      byte[] fitBytes, String activityId) {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add(
        "file",
        new ByteArrayResource(fitBytes) {
          @Override
          public String getFilename() {
            return "activity.fit";
          }
        });
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return restTemplate.exchange(
        "/api/tracks/fit/" + activityId,
        HttpMethod.PUT,
        new HttpEntity<>(body, headers),
        TrackController.TrackSummary.class);
  }

  private org.springframework.http.ResponseEntity<TrackController.TrackSummary> postFit(
      byte[] fitBytes, String filename) {
    var body = new LinkedMultiValueMap<String, Object>();
    body.add(
        "file",
        new ByteArrayResource(fitBytes) {
          @Override
          public String getFilename() {
            return filename;
          }
        });
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return restTemplate.exchange(
        "/api/tracks/fit",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        TrackController.TrackSummary.class);
  }

  private static ByteArrayResource emptyFitResource(String filename) {
    return new ByteArrayResource(new byte[0]) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  private static byte[] buildFit(Integer fileNumber) throws Exception {
    var encoder = new BufferEncoder(Fit.ProtocolVersion.V2_0);
    encoder.open();

    var fileId = new FileIdMesg();
    fileId.setType(com.garmin.fit.File.ACTIVITY);
    if (fileNumber != null) fileId.setNumber(fileNumber);
    encoder.write(fileId);

    var session = new SessionMesg();
    session.setStartTime(new DateTime(Date.from(Instant.parse("2024-06-01T08:00:00Z"))));
    session.setTotalElapsedTime(3600.0f);
    session.setTotalTimerTime(3500.0f);
    session.setTotalDistance(15000.0f);
    session.setTotalCalories(500);
    session.setAvgHeartRate((short) 145);
    session.setMaxHeartRate((short) 180);
    session.setAvgSpeed(4.1667f);
    session.setMaxSpeed(6.0f);
    session.setAvgPower(200);
    session.setNormalizedPower(210);
    session.setAvgCadence((short) 85);
    session.setSport(Sport.CYCLING);
    session.setSubSport(SubSport.ROAD);
    session.setTotalAscent(500);
    session.setTotalDescent(480);
    encoder.write(session);

    return encoder.close();
  }
}
