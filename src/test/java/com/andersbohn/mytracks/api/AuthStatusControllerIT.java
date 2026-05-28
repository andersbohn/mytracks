package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.andersbohn.mytracks.domain.UserRole;
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
class AuthStatusControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;

  @Test
  void returnsAuthenticatedUserFromMockFilter() {
    var response =
        restTemplate.getForEntity(
            "/api/auth/status", AuthStatusController.AuthStatusResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.authenticated()).isTrue();
    assertThat(body.user()).isNotNull();
    assertThat(body.user().email()).isEqualTo("test@example.com");
    assertThat(body.user().role()).isEqualTo(UserRole.GUEST);
    assertThat(body.loginUrl()).isNull();
    assertThat(body.googleClientId()).isEqualTo("test-client-id");
  }

  @Test
  void logout_invalidatesSession() {
    var response = restTemplate.postForEntity("/api/auth/logout", null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
