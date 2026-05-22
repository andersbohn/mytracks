package com.andersbohn.mytracks.security;

import com.andersbohn.mytracks.domain.User;
import com.andersbohn.mytracks.domain.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
  private final UserRepository userRepository;
  private final UserAuthProperties authProperties;

  public UserAuthService(UserRepository userRepository, UserAuthProperties authProperties) {
    this.userRepository = userRepository;
    this.authProperties = authProperties;
  }

  public User findOrRegister(
      String email, String displayName, String ssoProvider, String ssoSubject) {
    return userRepository
        .findBySsoProviderAndSsoSubject(ssoProvider, ssoSubject)
        .orElseGet(
            () ->
                userRepository.save(
                    new User(email, displayName, ssoProvider, ssoSubject, Instant.now())));
  }

  public boolean isAllowed(String email) {
    return authProperties.isAllowed(email);
  }
}
