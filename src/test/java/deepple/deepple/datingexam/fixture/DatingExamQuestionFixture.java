package deepple.deepple.datingexam.fixture;

import deepple.deepple.datingexam.domain.DatingExamQuestion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

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
        DatingExamQuestion question = DatingExamQuestion.create(subjectId, content);
        setField(question, "id", id);
        return question;
    }
}
