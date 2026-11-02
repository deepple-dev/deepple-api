package deepple.deepple.datingexam.adapter.encoding;

import deepple.deepple.datingexam.domain.dto.AnswerSubmitRequest;
import deepple.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatingExamEncoderTest {

    private final ObjectMapper realMapper = new ObjectMapper();

    @Test
    @DisplayName("encode()를 호출하면, 올바른 JSON 문자열을 반환한다.")
    void encodeShouldReturnCorrectJson() throws Exception {
        DatingExamEncoder encoder = new DatingExamEncoder(realMapper);

        // given
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(11L, 111L),
            new AnswerSubmitRequest(12L, 113L)
        ));

        // when
        String json = encoder.encode(request);

        // then
        assertThat(json).isNotBlank();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        Map<String, Object> map = realMapper.readValue(json, typeRef);


        assertThat(map)
            .containsKey("as");

        var asList = (List<?>) map.get("as");
        assertThat(asList).hasSize(2);

        var ansMap1 = (Map<String, Object>) asList.get(0);
        assertThat(ansMap1)
            .containsEntry("q", 11)
            .containsEntry("a", 111);

        var ansMap2 = (Map<String, Object>) asList.get(1);
        assertThat(ansMap2)
            .containsEntry("q", 12)
            .containsEntry("a", 113);
    }
}
