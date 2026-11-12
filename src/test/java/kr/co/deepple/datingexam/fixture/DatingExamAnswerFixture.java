package kr.co.deepple.datingexam.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamAnswer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatingExamAnswerFixture {

  private Long id;
  private long questionId = 1L;
  private String content = "연애고사 답변";
  private AnswerPersonalityType personalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;

  public static DatingExamAnswerFixture builder() {
    return new DatingExamAnswerFixture();
  }

  public DatingExamAnswer build() {
    DatingExamAnswer answer = DatingExamAnswer.of(questionId, content, personalityType);
    setField(answer, "id", id);
    return answer;
  }
}
