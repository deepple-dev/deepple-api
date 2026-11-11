package kr.co.deepple.interview.fixture;

import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import kr.co.deepple.interview.command.domain.question.InterviewQuestion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InterviewQuestionFixture {

    private Long id;
    private String content = "인터뷰 질문";
    private InterviewCategory category = InterviewCategory.PERSONAL;
    private boolean publicQuestion = true;

    public static InterviewQuestionFixture builder() {
        return new InterviewQuestionFixture();
    }

    public InterviewQuestion build() {
        InterviewQuestion question = InterviewQuestion.of(content, category, publicQuestion);
        setField(question, "id", id);
        return question;
    }
}
