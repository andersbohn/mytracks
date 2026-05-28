package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.domain.UserRepository;
import com.andersbohn.mytracks.domain.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

  private final UserRepository userRepository;

  public UserAdminController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<UserController.UserResponse>> listAll(
      @AuthenticationPrincipal OAuth2User principal) {
    if (!isAdmin(principal)) {
      return ResponseEntity.status(403).build();
    }
    var users =
        userRepository.findAll().stream()
            .map(
                u ->
                    new UserController.UserResponse(
                        u.getId(), u.getEmail(), u.getDisplayName(), u.getRole()))
            .toList();
    return ResponseEntity.ok(users);
  }

  @PatchMapping("/{id}/role")
  public ResponseEntity<UserController.UserResponse> changeRole(
      @AuthenticationPrincipal OAuth2User principal,
      @PathVariable UUID id,
      @RequestBody RoleChangeRequest body) {
    if (!isAdmin(principal)) {
      return ResponseEntity.status(403).build();
    }
    return userRepository
        .findById(id)
        .map(
            target -> {
              target.setRole(body.role());
              userRepository.save(target);
              return ResponseEntity.ok(
                  new UserController.UserResponse(
                      target.getId(),
                      target.getEmail(),
                      target.getDisplayName(),
                      target.getRole()));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private boolean isAdmin(OAuth2User principal) {
    String email = principal.getAttribute("email");
    return userRepository.findByEmail(email).map(u -> u.getRole() == UserRole.ADMIN).orElse(false);
  }

  public record RoleChangeRequest(UserRole role) {}
}
