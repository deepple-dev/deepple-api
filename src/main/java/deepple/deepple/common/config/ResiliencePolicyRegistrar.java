package deepple.deepple.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            cb.getEventPublisher()
                .onStateTransition(event -> log.warn(
                    "[CircuitBreaker 상태 전환] name={}, {} -> {}",
                    event.getCircuitBreakerName(),
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState()
                ))
                .onFailureRateExceeded(event -> log.warn(
                    "[CircuitBreaker 실패율 초과] name={}, failureRate={}%",
                    event.getCircuitBreakerName(),
                    event.getFailureRate()
                ))
                .onSlowCallRateExceeded(event -> log.warn(
                    "[CircuitBreaker 느린응답률 초과] name={}, slowCallRate={}%",
                    event.getCircuitBreakerName(),
                    event.getSlowCallRate()
                ));
        });
    }
}
