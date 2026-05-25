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
@RequestMapping("/api/auth")
public class AuthStatusController {

  static final String LOGIN_URL = "/oauth2/authorization/google";

  private final UserRepository userRepository;

  public AuthStatusController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/status")
  public ResponseEntity<AuthStatusResponse> status(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return ResponseEntity.ok(new AuthStatusResponse(false, null, LOGIN_URL));
    }
    String email = principal.getAttribute("email");
    return userRepository
        .findByEmail(email)
        .map(
            u ->
                ResponseEntity.ok(
                    new AuthStatusResponse(
                        true, new UserInfo(u.getId(), u.getEmail(), u.getDisplayName()), null)))
        .orElse(ResponseEntity.ok(new AuthStatusResponse(false, null, LOGIN_URL)));
  }

  public record UserInfo(UUID id, String email, String displayName) {}

  public record AuthStatusResponse(boolean authenticated, UserInfo user, String loginUrl) {}
}
