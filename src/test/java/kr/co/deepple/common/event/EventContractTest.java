package kr.co.deepple.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.tngtech.archunit.core.domain.JavaClass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.co.deepple.DeeppleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class EventContractTest {

  private static final Path CONTRACTS = Path.of("src/test/resources/contracts");
  private static final Pattern VERSION = Pattern.compile("v(\\d+)\\.json");
  private static final int COMPARED_VERSIONS = 2;

  @Autowired private JsonMapper jsonMapper;

  @ParameterizedTest(name = "{0}")
  @MethodSource("contracts")
  @DisplayName("옛 sample을 지금 이벤트 클래스로 읽을 수 있다.")
  void readContractWithCurrentClass(Path contract) throws Exception {
    Class<?> eventType = eventType(contract);

    assertThatNoException().isThrownBy(() -> jsonMapper.readValue(contract.toFile(), eventType));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("contracts")
  @DisplayName("지금 클래스로 쓴 JSON에 옛 sample의 칸이 같은 타입으로 모두 남아 있다.")
  void keepContractFieldsWhenWriting(Path contract) throws Exception {
    JsonNode expected = jsonMapper.readTree(contract.toFile());
    Object event = jsonMapper.treeToValue(expected, eventType(contract));
    JsonNode actual = jsonMapper.valueToTree(event);

    assertThat(fieldTypes(actual)).containsAllEntriesOf(fieldTypes(expected));
  }

  @Test
  @DisplayName("다른 모듈이 듣는 이벤트마다 sample이 있다.")
  void haveContractForEveryListenedEvent() throws IOException {
    ApplicationModules modules = ApplicationModules.of(DeeppleApplication.class);
    List<String> listenedEvents =
        modules.stream()
            .flatMap(module -> module.getEventsListenedTo(modules).stream())
            .map(JavaClass::getName)
            .distinct()
            .toList();

    assertThat(contractEventNames()).containsAll(listenedEvents);
  }

  static Stream<Path> contracts() throws IOException {
    try (Stream<Path> files = Files.walk(CONTRACTS)) {
      return files
          .filter(file -> VERSION.matcher(file.getFileName().toString()).matches())
          .collect(Collectors.groupingBy(Path::getParent))
          .values()
          .stream()
          .flatMap(versions ->
              versions.stream()
                  .sorted(Comparator.comparingInt(EventContractTest::version).reversed())
                  .limit(COMPARED_VERSIONS))
          .sorted();
    }
  }

  private static int version(Path contract) {
    Matcher matcher = VERSION.matcher(contract.getFileName().toString());
    matcher.matches();
    return Integer.parseInt(matcher.group(1));
  }

  private static String eventName(Path contract) {
    Path eventFolder = contract.getParent();
    String module = eventFolder.getParent().getFileName().toString();
    return "kr.co.deepple.%s.event.%s".formatted(module, eventFolder.getFileName());
  }

  private static Class<?> eventType(Path contract) throws ClassNotFoundException {
    return Class.forName(eventName(contract));
  }

  private static List<String> contractEventNames() throws IOException {
    return contracts().map(EventContractTest::eventName).distinct().toList();
  }

  private static Map<String, JsonNodeType> fieldTypes(JsonNode node) {
    Map<String, JsonNodeType> types = new TreeMap<>();
    collectFieldTypes(node, "", types);
    return types;
  }

  private static void collectFieldTypes(
      JsonNode node, String path, Map<String, JsonNodeType> types) {
    if (node.isObject()) {
      for (Map.Entry<String, JsonNode> field : node.properties()) {
        String fieldPath = path.isEmpty() ? field.getKey() : path + "." + field.getKey();
        types.put(fieldPath, field.getValue().getNodeType());
        collectFieldTypes(field.getValue(), fieldPath, types);
      }
    } else if (node.isArray()) {
      for (JsonNode element : node) {
        types.put(path + "[]", element.getNodeType());
        collectFieldTypes(element, path + "[]", types);
      }
    }
  }
}
