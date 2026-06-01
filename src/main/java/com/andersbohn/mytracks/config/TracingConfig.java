package com.andersbohn.mytracks.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class TracingConfig {

  @Bean
  ObservationPredicate tracingFilter() {
    return (name, context) -> {
      // suppress spring security filter chain child spans (filterchain before/after, authorize)
      if (name.startsWith("spring.security")) return false;
      // suppress actuator health check and OTLP proxy spans
      if (context instanceof ServerRequestObservationContext c) {
        String uri = c.getCarrier().getRequestURI();
        if (uri.contains("/actuator") || uri.contains("/api/otlp")) return false;
      }
      return true;
    };
  }
}
