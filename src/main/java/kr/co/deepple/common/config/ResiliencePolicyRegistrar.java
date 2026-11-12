package kr.co.deepple.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResiliencePolicyRegistrar implements SmartInitializingSingleton {

  private final RetryRegistry retryRegistry;
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final List<ResiliencePolicyConfigurer> configurers;

  @Override
  public void afterSingletonsInstantiated() {
    for (var c : configurers) {
      c.configure(retryRegistry, circuitBreakerRegistry);
    }
    registerCircuitBreakerEventListeners();
  }

  private void registerCircuitBreakerEventListeners() {
    circuitBreakerRegistry
        .getAllCircuitBreakers()
        .forEach(cb ->
            cb.getEventPublisher()
                .onStateTransition(event ->
                    log.atWarn()
                        .addKeyValue("circuitBreaker", event.getCircuitBreakerName())
                        .addKeyValue("fromState", event.getStateTransition().getFromState())
                        .addKeyValue("toState", event.getStateTransition().getToState())
                        .addKeyValue("failureRate", cb.getMetrics().getFailureRate())
                        .addKeyValue("slowCallRate", cb.getMetrics().getSlowCallRate())
                        .log("서킷 브레이커 상태 전환")));
  }
}
