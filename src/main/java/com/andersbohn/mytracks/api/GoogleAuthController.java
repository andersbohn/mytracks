package com.andersbohn.mytracks.api;

import com.andersbohn.mytracks.security.GoogleIdTokenVerifier;
import com.andersbohn.mytracks.security.InvalidTokenException;
import com.andersbohn.mytracks.security.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

  private final GoogleIdTokenVerifier tokenVerifier;
  private final UserAuthService userAuthService;

  public GoogleAuthController(
      GoogleIdTokenVerifier tokenVerifier, UserAuthService userAuthService) {
    this.tokenVerifier = tokenVerifier;
    this.userAuthService = userAuthService;
  }

  @PostMapping("/google")
  public ResponseEntity<UserController.UserResponse> register(
      @RequestBody GoogleIdTokenRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    var claims = tokenVerifier.verify(body.credential());
    var user =
        userAuthService.findOrRegister(claims.email(), claims.name(), "google", claims.sub());

    var principal =
        new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(
                "email", user.getEmail(),
                "name", user.getDisplayName() != null ? user.getDisplayName() : "",
                "sub", claims.sub()),
            "email");
    var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    new HttpSessionSecurityContextRepository().saveContext(context, request, response);

    return ResponseEntity.ok(
        new UserController.UserResponse(
            user.getId(), user.getEmail(), user.getDisplayName(), user.getRole()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Void> handleInvalidToken() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
