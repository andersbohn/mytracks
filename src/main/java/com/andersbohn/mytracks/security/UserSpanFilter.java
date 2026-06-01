package com.andersbohn.mytracks.security;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

/** Stamps the authenticated user's email onto the current span. */
public class UserSpanFilter extends OncePerRequestFilter {

  private final Tracer tracer;

  public UserSpanFilter(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof OAuth2User user) {
      String email = user.getAttribute("email");
      var span = tracer.currentSpan();
      if (email != null && span != null) {
        span.tag("enduser.id", email);
      }
    }
    chain.doFilter(request, response);
  }
}
