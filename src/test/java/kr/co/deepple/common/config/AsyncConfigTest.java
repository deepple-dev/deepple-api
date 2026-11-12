package kr.co.deepple.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.logging.LogContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AsyncConfigTest {

  private final ThreadPoolTaskExecutor executor =
      (ThreadPoolTaskExecutor) new AsyncConfig().getAsyncExecutor();

  @AfterEach
  void tearDown() {
    executor.shutdown();
    MDC.clear();
  }

  @Test
  @DisplayName("비동기 작업에 요청 스레드의 로그 문맥(MDC)을 전달한다.")
  void propagateLogContext() throws Exception {
    MDC.put(LogContext.USER_ID, "42");
    MDC.put(LogContext.HTTP_ROUTE, "/match/{id}");
    CompletableFuture<Map<String, String>> captured = new CompletableFuture<>();

    executor.execute(() -> captured.complete(MDC.getCopyOfContextMap()));

    assertThat(captured.get(5, TimeUnit.SECONDS))
        .containsEntry(LogContext.USER_ID, "42")
        .containsEntry(LogContext.HTTP_ROUTE, "/match/{id}");
  }

  @Test
  @DisplayName("비동기 작업이 끝나면 작업 스레드의 로그 문맥을 지운다.")
  void clearLogContextAfterTask() throws Exception {
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    MDC.put(LogContext.USER_ID, "42");
    CompletableFuture<String> firstThread = new CompletableFuture<>();
    executor.execute(() -> firstThread.complete(Thread.currentThread().getName()));
    firstThread.get(5, TimeUnit.SECONDS);
    MDC.remove(LogContext.USER_ID);
    CompletableFuture<String> secondThread = new CompletableFuture<>();
    CompletableFuture<String> captured = new CompletableFuture<>();

    executor.execute(() -> {
      secondThread.complete(Thread.currentThread().getName());
      captured.complete(MDC.get(LogContext.USER_ID));
    });

    assertThat(secondThread.get(5, TimeUnit.SECONDS)).isEqualTo(firstThread.get());
    assertThat(captured.get(5, TimeUnit.SECONDS)).isNull();
  }

  @Test
  @DisplayName("비동기 작업 실패 로그에 이벤트 이름과 이벤트의 로그 속성을 남긴다.")
  void logEventAttributesOnFailure() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(AsyncConfig.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    Method method = TestHandler.class.getMethod("handle", TestEvent.class);

    try {
      new AsyncConfig()
          .getAsyncUncaughtExceptionHandler()
          .handleUncaughtException(new IllegalStateException("실패"), method, new TestEvent(7L));
    } finally {
      logger.detachAppender(appender);
    }

    ILoggingEvent logged = appender.list.getFirst();
    assertThat(logged.getMDCPropertyMap()).containsEntry(LogContext.EVENT_ACTION, "TestEvent");
    assertThat(logged.getKeyValuePairs())
        .extracting(pair -> pair.key + "=" + pair.value)
        .containsExactlyInAnyOrder("code.function.name=TestHandler.handle", "memberId=7");
    assertThat(MDC.get(LogContext.EVENT_ACTION)).isNull();
  }

  @RequiredArgsConstructor
  static class TestEvent extends Event {

    private final long memberId;

    @Override
    public Map<String, Object> logAttributes() {
      return Map.of("memberId", memberId);
    }
  }

  static class TestHandler {

    public void handle(TestEvent event) {}
  }
}
