package kr.co.deepple.datingexam.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.EnumMap;
import java.util.Map;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatingExamSubmitResultFixture {

  private Long id;
  private long memberId = 1L;
  private Map<AnswerPersonalityType, Integer> counts = new EnumMap<>(AnswerPersonalityType.class);

  public static DatingExamSubmitResultFixture builder() {
    return new DatingExamSubmitResultFixture();
  }

  public DatingExamSubmitResult build() {
    DatingExamSubmitResult result = DatingExamSubmitResult.from(memberId);
    result.addCounts(counts);
    setField(result, "id", id);
    return result;
  }
}
