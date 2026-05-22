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
class UserRepositoryIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired UserRepository userRepository;

  @Test
  void findByEmail() {
    var user =
        userRepository.save(
            new User("alice@example.com", "Alice", "google", "google-sub-1", Instant.now()));
    assertThat(userRepository.findByEmail("alice@example.com"))
        .map(User::getId)
        .contains(user.getId());
  }

  @Test
  void findBySsoProviderAndSsoSubject() {
    var user =
        userRepository.save(
            new User("bob@example.com", "Bob", "google", "google-sub-2", Instant.now()));
    assertThat(userRepository.findBySsoProviderAndSsoSubject("google", "google-sub-2"))
        .map(User::getId)
        .contains(user.getId());
  }
}
