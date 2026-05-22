package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.UserRepository;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(@AuthenticationPrincipal OAuth2User principal) {
    String email = principal.getAttribute("email");
    return userRepository
        .findByEmail(email)
        .map(u -> ResponseEntity.ok(new UserResponse(u.getId(), u.getEmail(), u.getDisplayName())))
        .orElse(ResponseEntity.notFound().build());
  }

  public record UserResponse(UUID id, String email, String displayName) {}
}
