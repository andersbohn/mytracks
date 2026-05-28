package com.andersbohn.mytracks.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserAdminControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired TestRestTemplate restTemplate;
  @Autowired UserRepository userRepository;

  private User mockUser;

  @BeforeEach
  void setUp() {
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
    mockUser.setRole(UserRole.GUEST);
    mockUser = userRepository.save(mockUser);
  }

  @Test
  void listAll_asGuest_returns403() {
    var response =
        restTemplate.getForEntity("/api/admin/users", UserController.UserResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void listAll_asAdmin_returnsUsers() {
    mockUser.setRole(UserRole.ADMIN);
    userRepository.save(mockUser);

    var response =
        restTemplate.getForEntity("/api/admin/users", UserController.UserResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void changeRole_asAdmin_updatesRole() {
    mockUser.setRole(UserRole.ADMIN);
    userRepository.save(mockUser);

    var target =
        userRepository.save(
            new User("other@example.com", "Other", "mock", "other@example.com", Instant.now()));

    var body = new UserAdminController.RoleChangeRequest(UserRole.USER);
    var response =
        restTemplate.exchange(
            "/api/admin/users/" + target.getId() + "/role",
            HttpMethod.PATCH,
            new HttpEntity<>(body),
            UserController.UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().role()).isEqualTo(UserRole.USER);
    assertThat(userRepository.findById(target.getId()).orElseThrow().getRole())
        .isEqualTo(UserRole.USER);
  }

  @Test
  void changeRole_asNonAdmin_returns403() {
    var body = new UserAdminController.RoleChangeRequest(UserRole.USER);
    var response =
        restTemplate.exchange(
            "/api/admin/users/" + mockUser.getId() + "/role",
            HttpMethod.PATCH,
            new HttpEntity<>(body),
            UserController.UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void changeRole_unknownUser_returns404() {
    mockUser.setRole(UserRole.ADMIN);
    userRepository.save(mockUser);

    var body = new UserAdminController.RoleChangeRequest(UserRole.USER);
    var response =
        restTemplate.exchange(
            "/api/admin/users/" + UUID.randomUUID() + "/role",
            HttpMethod.PATCH,
            new HttpEntity<>(body),
            UserController.UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
