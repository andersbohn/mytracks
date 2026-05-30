package com.andersbohn.mytracks.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class TracingConfig {

  @Bean
  ObservationPredicate noActuatorTracing() {
    return (name, context) ->
        !(context instanceof ServerRequestObservationContext c
            && c.getCarrier().getRequestURI().contains("/actuator"));
  }
}
