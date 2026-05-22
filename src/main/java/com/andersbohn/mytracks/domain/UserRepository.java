package com.andersbohn.mytracks.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  Optional<User> findBySsoProviderAndSsoSubject(String ssoProvider, String ssoSubject);
}
