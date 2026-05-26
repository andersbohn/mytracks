package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "auth.mock-email=")
@Testcontainers
class AuthStatusUnauthenticatedIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;

  @Test
  void returnsUnauthenticatedWithLoginUrl() {
    var response =
        restTemplate.getForEntity(
            "/api/auth/status", AuthStatusController.AuthStatusResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.authenticated()).isFalse();
    assertThat(body.user()).isNull();
    assertThat(body.loginUrl()).isEqualTo(AuthStatusController.LOGIN_URL);
    assertThat(body.googleClientId()).isNotNull();
  }
}
