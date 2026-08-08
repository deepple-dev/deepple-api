package deepple.deepple.member.query.introduction.intra;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PersonalityIntroductionRedisRepository가 주입받는 ObjectMapper는 Spring Boot 자동 구성 빈이다.
 * Jackson2ObjectMapperBuilder로 동일하게 구성해 LocalDateTime(openedAt)이 실제로 직렬화/역직렬화되는지 검증한다.
 */
class PersonalityIntroductionOpenStateSerializationTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    @DisplayName("openedAt을 포함해 직렬화/역직렬화가 왕복된다.")
    void roundTripsWithOpenedAt() throws Exception {
        LocalDateTime openedAt = LocalDateTime.of(2026, 8, 8, 12, 0, 0);
        PersonalityIntroductionOpenState state =
            new PersonalityIntroductionOpenState(AnswerPersonalityType.STIMULATING_ADVENTURER, List.of(1L, 2L), openedAt);

        String json = objectMapper.writeValueAsString(state);
        PersonalityIntroductionOpenState deserialized = objectMapper.readValue(json, PersonalityIntroductionOpenState.class);

        assertThat(deserialized).isEqualTo(state);
        assertThat(deserialized.openedAt()).isEqualTo(openedAt);
    }

    @Test
    @DisplayName("openedAt 필드가 없는 배포 이전 레거시 JSON은 openedAt=null로 역직렬화된다.")
    void legacyJsonWithoutOpenedAtDeserializesWithNullOpenedAt() throws Exception {
        String legacyJson = """
            {"type":"STIMULATING_ADVENTURER","memberIds":[1,2]}""";

        PersonalityIntroductionOpenState deserialized =
            objectMapper.readValue(legacyJson, PersonalityIntroductionOpenState.class);

        assertThat(deserialized.type()).isEqualTo(AnswerPersonalityType.STIMULATING_ADVENTURER);
        assertThat(deserialized.memberIds()).isEqualTo(List.of(1L, 2L));
        assertThat(deserialized.openedAt()).isNull();
    }
}
