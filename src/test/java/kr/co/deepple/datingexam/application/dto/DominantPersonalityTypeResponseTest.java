package kr.co.deepple.datingexam.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DominantPersonalityTypeResponseTest {

  @Test
  @DisplayName("대표 유형과 함께 잘 맞는 유형 2개를 응답한다.")
  void respondCompatibleTypes() {
    DominantPersonalityTypeResponse response =
        DominantPersonalityTypeResponse.from(
            DatingExamSubmitResultFixture.builder()
                .counts(Map.of(AnswerPersonalityType.DEVOTED_ROMANTIC, 3))
                .build());

    assertThat(response.personalityType()).isEqualTo("DEVOTED_ROMANTIC");
    assertThat(response.compatibleTypes())
        .containsExactly("REALISTIC_SHELTER", "STIMULATING_ADVENTURER");
  }
}
