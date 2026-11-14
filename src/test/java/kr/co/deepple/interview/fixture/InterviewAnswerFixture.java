package kr.co.deepple.interview.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.interview.domain.InterviewAnswer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InterviewAnswerFixture {

  private Long id;
  private long questionId = 1L;
  private long memberId = 1L;
  private String content = "인터뷰 답변";

  public static InterviewAnswerFixture builder() {
    return new InterviewAnswerFixture();
  }

  public InterviewAnswer build() {
    InterviewAnswer answer = InterviewAnswer.of(questionId, memberId, content);
    setField(answer, "id", id);
    return answer;
  }
}
