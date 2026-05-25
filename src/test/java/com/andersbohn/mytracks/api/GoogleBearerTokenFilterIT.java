package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.andersbohn.mytracks.security.GoogleIdTokenVerifier;
import com.andersbohn.mytracks.security.GoogleTokenClaims;
import com.andersbohn.mytracks.security.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "auth.mock-email=")
@Testcontainers
class GoogleBearerTokenFilterIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;
  @MockBean GoogleIdTokenVerifier tokenVerifier;

  @Test
  void validBearerToken_authenticatesAndReturnsUser() {
    when(tokenVerifier.verify("valid-bearer"))
        .thenReturn(new GoogleTokenClaims("sub-bearer", "bearer@example.com", "Bearer User"));

    var headers = new HttpHeaders();
    headers.setBearerAuth("valid-bearer");
    var response =
        restTemplate.exchange(
            "/api/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserController.UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo("bearer@example.com");
    assertThat(response.getBody().displayName()).isEqualTo("Bearer User");
  }

  @Test
  void invalidBearerToken_onProtectedEndpoint_returns401() {
    when(tokenVerifier.verify("bad-bearer")).thenThrow(new InvalidTokenException("bad"));

    var headers = new HttpHeaders();
    headers.setBearerAuth("bad-bearer");
    var response =
        restTemplate.exchange("/api/me", HttpMethod.GET, new HttpEntity<>(headers), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void invalidBearerToken_onPublicEndpoint_returns200Unauthenticated() {
    when(tokenVerifier.verify("bad-bearer")).thenThrow(new InvalidTokenException("bad"));

    var headers = new HttpHeaders();
    headers.setBearerAuth("bad-bearer");
    var response =
        restTemplate.exchange(
            "/api/auth/status",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            AuthStatusController.AuthStatusResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().authenticated()).isFalse();
  }

  @Test
  void noBearerToken_noSession_returns401() {
    var response = restTemplate.getForEntity("/api/me", Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
