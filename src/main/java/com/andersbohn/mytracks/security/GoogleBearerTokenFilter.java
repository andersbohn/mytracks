package com.andersbohn.mytracks.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

public class GoogleBearerTokenFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(GoogleBearerTokenFilter.class);

  private final GoogleIdTokenVerifier tokenVerifier;
  private final UserAuthService userAuthService;

  public GoogleBearerTokenFilter(
      GoogleIdTokenVerifier tokenVerifier, UserAuthService userAuthService) {
    this.tokenVerifier = tokenVerifier;
    this.userAuthService = userAuthService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);
    try {
      var claims = tokenVerifier.verify(token);
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
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    } catch (InvalidTokenException e) {
      // Clear context and chain — Spring Security's entry point handles 401 for protected
      // endpoints, while public endpoints (permitAll) still respond normally.
      log.warn("Bearer token verification failed: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(request, response);
  }
}
