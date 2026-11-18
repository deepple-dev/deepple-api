package kr.co.deepple.recommendation.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class PersonalityIntroductionOpenStateSerializationTest {

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("openedAt 필드가 없는 배포 이전 레거시 JSON은 openedAt=null로 역직렬화된다.")
  void legacyJsonWithoutOpenedAtDeserializesWithNullOpenedAt() throws Exception {
    String legacyJson =
        """
        {"type":"STIMULATING_ADVENTURER","memberIds":[1,2]}\
        """;

    PersonalityIntroductionOpenState deserialized =
        objectMapper.readValue(legacyJson, PersonalityIntroductionOpenState.class);

    assertThat(deserialized.type()).isEqualTo("STIMULATING_ADVENTURER");
    assertThat(deserialized.memberIds()).isEqualTo(List.of(1L, 2L));
    assertThat(deserialized.openedAt()).isNull();
  }
}
