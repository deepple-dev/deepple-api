package kr.co.deepple.common.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservabilityConfig {

  @Bean
  public ObservationPredicate noActuatorObservations() {
    return (name, context) ->
        !(context instanceof ServerRequestObservationContext server
            && server.getCarrier().getRequestURI().startsWith("/actuator"));
  }
}
