package kr.co.deepple.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionalEventListener;

class EventLogContextAspectTest {

  private final SampleHandler handler = createProxy();

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  @DisplayName("이벤트 핸들러가 도는 동안 event.action에 이벤트 이름을 넣는다.")
  void putEventNameDuringEventListener() {
    handler.handle(new SampleEvent());

    assertThat(handler.getCapturedAction()).isEqualTo("SampleEvent");
    assertThat(MDC.get(LogContext.EVENT_ACTION)).isNull();
  }

  @Test
  @DisplayName("트랜잭션 이벤트 핸들러에도 이벤트 이름을 넣고, 끝나면 이전 값으로 되돌린다.")
  void restorePreviousActionAfterTransactionalEventListener() {
    MDC.put(LogContext.EVENT_ACTION, "OuterEvent");

    handler.handleAfterCommit(new SampleEvent());

    assertThat(handler.getCapturedAction()).isEqualTo("SampleEvent");
    assertThat(MDC.get(LogContext.EVENT_ACTION)).isEqualTo("OuterEvent");
  }

  @Test
  @DisplayName("스케줄러 작업이 도는 동안 event.action에 클래스와 메서드 이름을 넣는다.")
  void putJobNameDuringScheduledMethod() {
    handler.run();

    assertThat(handler.getCapturedAction()).isEqualTo("SampleHandler.run");
  }

  private SampleHandler createProxy() {
    AspectJProxyFactory factory = new AspectJProxyFactory(new SampleHandler());
    factory.setProxyTargetClass(true);
    factory.addAspect(new EventLogContextAspect());
    return factory.getProxy();
  }

  record SampleEvent() {}

  static class SampleHandler {

    private String capturedAction;

    public String getCapturedAction() {
      return capturedAction;
    }

    @EventListener
    public void handle(SampleEvent event) {
      capturedAction = MDC.get(LogContext.EVENT_ACTION);
    }

    @TransactionalEventListener
    public void handleAfterCommit(SampleEvent event) {
      capturedAction = MDC.get(LogContext.EVENT_ACTION);
    }

    @Scheduled(fixedDelay = 1000)
    public void run() {
      capturedAction = MDC.get(LogContext.EVENT_ACTION);
    }
  }
}
