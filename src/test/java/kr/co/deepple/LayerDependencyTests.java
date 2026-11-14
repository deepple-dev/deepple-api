package kr.co.deepple;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LayerDependencyTests {

  private static final String DOMAIN = "kr.co.deepple..domain..";
  private static final String APPLICATION = "kr.co.deepple..application..";
  private static final String ADAPTER = "kr.co.deepple..adapter..";
  private static final String ADAPTER_IN = "kr.co.deepple..adapter.in..";
  private static final String ADAPTER_OUT = "kr.co.deepple..adapter.out..";
  private static final String PERSISTENCE_ADAPTER = "kr.co.deepple..adapter.out.persistence..";

  private final JavaClasses classes =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("kr.co.deepple");

  @Test
  @DisplayName("domain은 application과 adapter에 의존하지 않는다.")
  void domainDoesNotDependOnOuterLayers() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(APPLICATION, ADAPTER)
        .check(classes);
  }

  @Test
  @DisplayName("application은 adapter/out/persistence에 의존하지 않는다.")
  void applicationDoesNotDependOnPersistenceAdapter() {
    noClasses()
        .that()
        .resideInAPackage(APPLICATION)
        .should()
        .dependOnClassesThat()
        .resideInAPackage(PERSISTENCE_ADAPTER)
        .check(classes);
  }

  @Test
  @DisplayName("adapter/in은 adapter/out에 의존하지 않는다.")
  void inboundAdapterDoesNotDependOnOutboundAdapter() {
    noClasses()
        .that()
        .resideInAPackage(ADAPTER_IN)
        .should()
        .dependOnClassesThat()
        .resideInAPackage(ADAPTER_OUT)
        .check(classes);
  }

  @Test
  @DisplayName("adapter/out은 adapter/in에 의존하지 않는다.")
  void outboundAdapterDoesNotDependOnInboundAdapter() {
    noClasses()
        .that()
        .resideInAPackage(ADAPTER_OUT)
        .should()
        .dependOnClassesThat()
        .resideInAPackage(ADAPTER_IN)
        .check(classes);
  }
}
