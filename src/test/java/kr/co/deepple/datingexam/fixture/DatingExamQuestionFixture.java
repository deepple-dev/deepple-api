package kr.co.deepple.datingexam.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.datingexam.domain.DatingExamQuestion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatingExamQuestionFixture {

  private Long id;
  private long subjectId = 1L;
  private String content = "연애고사 질문";

  public static DatingExamQuestionFixture builder() {
    return new DatingExamQuestionFixture();
  }

  public DatingExamQuestion build() {
    DatingExamQuestion question = DatingExamQuestion.of(subjectId, content);
    setField(question, "id", id);
    return question;
  }
}
