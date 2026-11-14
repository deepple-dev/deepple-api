package kr.co.deepple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

  private final ApplicationModules modules = ApplicationModules.of(DeeppleApplication.class);

  @Test
  @DisplayName("모듈 경계를 지킨다.")
  void verifyModules() {
    modules.verify();
  }

  @Test
  @DisplayName("모듈 문서를 만든다.")
  void writeDocumentation() {
    new Documenter(modules).writeDocumentation();
  }
}
