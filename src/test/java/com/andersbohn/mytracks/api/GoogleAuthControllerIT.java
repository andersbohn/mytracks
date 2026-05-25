package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.security.GoogleIdTokenVerifier;
import com.andersbohn.mytracks.security.GoogleTokenClaims;
import com.andersbohn.mytracks.security.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class GoogleAuthControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;
  @Autowired UserRepository userRepository;
  @MockBean GoogleIdTokenVerifier tokenVerifier;

  @Test
  void validToken_createsUserAndReturnsUserResponse() {
    when(tokenVerifier.verify("valid-token"))
        .thenReturn(new GoogleTokenClaims("sub-new", "new@example.com", "New User"));

    var response =
        restTemplate.postForEntity(
            "/api/auth/google",
            new GoogleIdTokenRequest("valid-token"),
            UserController.UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.email()).isEqualTo("new@example.com");
    assertThat(body.displayName()).isEqualTo("New User");
    assertThat(body.id()).isNotNull();

    assertThat(userRepository.findByEmail("new@example.com")).isPresent();
  }

  @Test
  void validToken_existingUser_returnsExistingUser() {
    when(tokenVerifier.verify("valid-token-existing"))
        .thenReturn(new GoogleTokenClaims("sub-existing", "existing@example.com", "Existing User"));

    restTemplate.postForEntity(
        "/api/auth/google",
        new GoogleIdTokenRequest("valid-token-existing"),
        UserController.UserResponse.class);
    var second =
        restTemplate.postForEntity(
            "/api/auth/google",
            new GoogleIdTokenRequest("valid-token-existing"),
            UserController.UserResponse.class);

    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(userRepository.findBySsoProviderAndSsoSubject("google", "sub-existing")).isPresent();
  }

  @Test
  void invalidToken_returns401() {
    when(tokenVerifier.verify("bad-token")).thenThrow(new InvalidTokenException("bad"));

    var response =
        restTemplate.postForEntity(
            "/api/auth/google", new GoogleIdTokenRequest("bad-token"), Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
