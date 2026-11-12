package kr.co.deepple.datingexam.fixture;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatingExamSubmitFixture {

  private Long id;
  private long memberId = 1L;
  private long subjectId = 1L;
  private String answers = "{\"as\":[{\"q\":1,\"a\":1}]}";

  public static DatingExamSubmitFixture builder() {
    return new DatingExamSubmitFixture();
  }

  public DatingExamSubmit build() {
    DatingExamSubmit submit = instantiateClass(DatingExamSubmit.class);
    setField(submit, "id", id);
    setField(submit, "memberId", memberId);
    setField(submit, "subjectId", subjectId);
    setField(submit, "answers", answers);
    return submit;
  }
}
