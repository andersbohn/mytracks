package com.andersbohn.mytracks.security;

import com.andersbohn.mytracks.config.AppProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final UserAuthProperties authProperties;
  private final AppProperties appProperties;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final UserAuthService userAuthService;
  private final GoogleIdTokenVerifier tokenVerifier;

  public SecurityConfig(
      UserAuthProperties authProperties,
      AppProperties appProperties,
      CustomOAuth2UserService customOAuth2UserService,
      UserAuthService userAuthService,
      GoogleIdTokenVerifier tokenVerifier) {
    this.authProperties = authProperties;
    this.appProperties = appProperties;
    this.customOAuth2UserService = customOAuth2UserService;
    this.userAuthService = userAuthService;
    this.tokenVerifier = tokenVerifier;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/health/**",
                        "/api/auth/status",
                        "/api/auth/google",
                        "/api/auth/logout")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    http.addFilterBefore(
            new MockAuthFilter(userAuthService, authProperties),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            new GoogleBearerTokenFilter(tokenVerifier, userAuthService), MockAuthFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(
                        new SimpleUrlAuthenticationSuccessHandler(appProperties.getFrontendUrl()))
                    .failureHandler(
                        new SimpleUrlAuthenticationFailureHandler(
                            appProperties.getFrontendUrl() + "?error=auth_failed")));

    return http.build();
  }

  private CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(appProperties.getCorsAllowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
