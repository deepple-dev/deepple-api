package kr.co.deepple.common.config;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.exception.ErrorType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

  private static final int MAX_RETRIES = 4;
  private static final BackOff NO_RETRY = new FixedBackOff(0L, 0L);

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
    ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRIES);

    DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), backOff);
    errorHandler.addNotRetryableExceptions(
        IllegalArgumentException.class, IllegalStateException.class);
    errorHandler.setBackOffFunction(
        (record, exception) -> isPermanent(exception) ? NO_RETRY : null);
    return errorHandler;
  }

  private static boolean isPermanent(Exception exception) {
    return exception instanceof DeeppleException deeppleException
        && deeppleException.getErrorCode().getType() != ErrorType.UNAVAILABLE;
  }
}
