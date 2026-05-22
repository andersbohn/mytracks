package com.andersbohn.mytracks.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

/** No-op when auth.mock.email is unset; auto-authenticates for local runs when it is. */
public class MockAuthFilter extends OncePerRequestFilter {
  private final UserAuthService userAuthService;
  private final UserAuthProperties authProperties;

  public MockAuthFilter(UserAuthService userAuthService, UserAuthProperties authProperties) {
    this.userAuthService = userAuthService;
    this.authProperties = authProperties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (authProperties.hasMockEmail()
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      String mockEmail = authProperties.getMockEmail();
      var user = userAuthService.findOrRegister(mockEmail, "Local Dev", "mock", mockEmail);
      var principal =
          new DefaultOAuth2User(
              List.of(new SimpleGrantedAuthority("ROLE_USER")),
              Map.of(
                  "email",
                  mockEmail,
                  "name",
                  user.getDisplayName() != null ? user.getDisplayName() : "Local Dev",
                  "sub",
                  mockEmail),
              "email");
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    chain.doFilter(request, response);
  }
}
