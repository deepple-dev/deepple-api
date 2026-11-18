package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import kr.co.deepple.DeeppleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.events.Externalized;

class EventExternalizationTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("listenedEvents")
  @DisplayName("다른 모듈이 듣는 이벤트는 발행 모듈 토픽으로 키를 붙여 Kafka에 보낸다.")
  void externalizeToPublisherTopicWithKey(String event, String publisher)
      throws ClassNotFoundException {
    Externalized externalized = Class.forName(event).getAnnotation(Externalized.class);

    assertThat(externalized).isNotNull();
    assertThat(externalized.value()).startsWith(publisher + "::#{");
  }

  static Stream<Arguments> listenedEvents() {
    ApplicationModules modules = ApplicationModules.of(DeeppleApplication.class);
    return modules.stream()
        .flatMap(module -> module.getEventsListenedTo(modules).stream())
        .distinct()
        .map(event ->
            Arguments.of(
                event.getName(),
                modules
                    .getModuleByType(event)
                    .map(ApplicationModule::getIdentifier)
                    .orElseThrow()
                    .toString()));
  }
}
