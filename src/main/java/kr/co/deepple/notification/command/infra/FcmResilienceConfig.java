package kr.co.deepple.notification.command.infra;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import kr.co.deepple.common.config.ResiliencePolicyConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmResilienceConfig implements ResiliencePolicyConfigurer {

  public static final String RETRY_POLICY_NAME = "fcmRetry";
  public static final String CIRCUIT_BREAKER_POLICY_NAME = "fcmCircuitBreaker";

  private static final int MAX_ATTEMPTS = 3; // 최초 1회 + 재시도 2회
  private static final int RETRY_INITIAL_WAIT_MILLIS = 500;
  private static final double RETRY_MULTIPLIER = 2.0; // 지수 백오프 배수
  private static final double RETRY_JITTER = 0.3; // ±30% 지터

  private static final int CB_SLIDING_WINDOW_SIZE = 60; // 60초 시간 윈도우
  private static final int CB_MINIMUM_NUMBER_OF_CALLS = 10; // 최소 10회 호출 후 실패율 계산
  private static final float CB_FAILURE_RATE_THRESHOLD = 50f; // 실패율 50% 이상시 차단
  private static final float CB_SLOW_CALL_RATE_THRESHOLD = 50f; // 느린 응답 50% 이상시 차단
  private static final int CB_SLOW_CALL_DURATION_MILLIS = 2000; // 2초 이상을 느린 응답으로 판단
  private static final int CB_WAIT_DURATION_IN_OPEN_MILLIS = 7000; // OPEN 상태에서 7초 대기
  private static final int CB_PERMITTED_CALLS_IN_HALF_OPEN = 3; // HALF_OPEN에서 3회 테스트 호출
  private static final int CB_MAX_WAIT_IN_HALF_OPEN_MILLIS = 5000; // HALF_OPEN에서 최대 5초 대기

  @Override
  public void configure(RetryRegistry r, CircuitBreakerRegistry c) {
    r.retry(RETRY_POLICY_NAME, retryConfig());
    c.circuitBreaker(CIRCUIT_BREAKER_POLICY_NAME, circuitBreakerConfig());
  }

  private RetryConfig retryConfig() {
    return RetryConfig.custom()
        .maxAttempts(MAX_ATTEMPTS)
        .intervalFunction(
            IntervalFunction.ofExponentialRandomBackoff(
                Duration.ofMillis(RETRY_INITIAL_WAIT_MILLIS), RETRY_MULTIPLIER, RETRY_JITTER))
        .retryOnException(this::shouldRetry)
        .build();
  }

  private boolean shouldRetry(Throwable throwable) {
    // 네트워크 일시적 장애 재시도
    if (throwable instanceof ConnectException
        || throwable instanceof UnknownHostException
        || throwable instanceof SocketTimeoutException) {
      return true;
    }

    // FCM 서버 오류만 재시도
    if (throwable instanceof FirebaseMessagingException fme) {
      MessagingErrorCode errorCode = fme.getMessagingErrorCode();
      if (errorCode == null) {
        return false;
      }

      return switch (errorCode) {
        case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED -> true;
        default -> false;
      };
    }

    return false;
  }

  private CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .slidingWindowType(TIME_BASED)
        .slidingWindowSize(CB_SLIDING_WINDOW_SIZE)
        .minimumNumberOfCalls(CB_MINIMUM_NUMBER_OF_CALLS)
        .failureRateThreshold(CB_FAILURE_RATE_THRESHOLD)
        .slowCallRateThreshold(CB_SLOW_CALL_RATE_THRESHOLD)
        .slowCallDurationThreshold(Duration.ofMillis(CB_SLOW_CALL_DURATION_MILLIS))
        .waitDurationInOpenState(Duration.ofMillis(CB_WAIT_DURATION_IN_OPEN_MILLIS))
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .permittedNumberOfCallsInHalfOpenState(CB_PERMITTED_CALLS_IN_HALF_OPEN)
        .maxWaitDurationInHalfOpenState(Duration.ofMillis(CB_MAX_WAIT_IN_HALF_OPEN_MILLIS))
        .recordException(this::shouldRecordAsFailure)
        .build();
  }

  private boolean shouldRecordAsFailure(Throwable throwable) {
    // 네트워크 일시적 장애 실패로 기록
    if (throwable instanceof ConnectException
        || throwable instanceof UnknownHostException
        || throwable instanceof SocketTimeoutException) {
      return true;
    }

    // FCM 서버 장애만 실패로 기록
    if (throwable instanceof FirebaseMessagingException fme) {
      MessagingErrorCode errorCode = fme.getMessagingErrorCode();
      if (errorCode == null) {
        return false;
      }

      return switch (errorCode) {
        case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED -> true;
        default -> false;
      };
    }

    return false;
  }
}
