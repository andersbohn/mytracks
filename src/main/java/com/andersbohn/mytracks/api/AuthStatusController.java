package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthStatusController {

  static final String LOGIN_URL = "/oauth2/authorization/google";

  private final UserRepository userRepository;
  private final String googleClientId;

  public AuthStatusController(
      UserRepository userRepository,
      @Value("${spring.security.oauth2.client.registration.google.client-id}")
          String googleClientId) {
    this.userRepository = userRepository;
    this.googleClientId = googleClientId;
  }

  @GetMapping("/status")
  public ResponseEntity<AuthStatusResponse> status(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return ResponseEntity.ok(new AuthStatusResponse(false, null, LOGIN_URL, googleClientId));
    }
    String email = principal.getAttribute("email");
    return userRepository
        .findByEmail(email)
        .map(
            u ->
                ResponseEntity.ok(
                    new AuthStatusResponse(
                        true,
                        new UserInfo(u.getId(), u.getEmail(), u.getDisplayName()),
                        null,
                        googleClientId)))
        .orElse(ResponseEntity.ok(new AuthStatusResponse(false, null, LOGIN_URL, googleClientId)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    new SecurityContextLogoutHandler().logout(request, response, auth);
    return ResponseEntity.ok().build();
  }

  public record UserInfo(UUID id, String email, String displayName) {}

  public record AuthStatusResponse(
      boolean authenticated, UserInfo user, String loginUrl, String googleClientId) {}
}
