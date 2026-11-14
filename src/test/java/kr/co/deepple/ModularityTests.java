package kr.co.deepple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

class ModularityTests {

  private static final int MAX_VIOLATIONS = 1050;

  private final ApplicationModules modules = ApplicationModules.of(DeeppleApplication.class);

  @Test
  @DisplayName("모듈 구조를 출력한다.")
  void printModules() {
    modules.forEach(System.out::println);
  }

  @Test
  @DisplayName("모듈 경계 위반 수가 상한을 넘지 않는다.")
  void keepViolationsWithinLimit() {
    Violations violations = modules.detectViolations();

    assertThat(violations.getMessages().size())
        .as("모듈 경계 위반이 늘었다. 다른 모듈의 내부 패키지를 새로 참조했는지 확인한다.")
        .isLessThanOrEqualTo(MAX_VIOLATIONS);
  }
}
