package kr.co.deepple.interview.command.domain.answer;

import kr.co.deepple.common.entity.BaseEntity;
import jakarta.persistence.*;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.command.domain.InterviewErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "interview_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    private Long questionId;

    private Long memberId;

    @Getter
    private String content;

    private InterviewAnswer(Long questionId, Long memberId, String content) {
        setQuestionId(questionId);
        setMemberId(memberId);
        setContent(content);
    }

    public static InterviewAnswer of(Long questionId, Long memberId, String content) {
        return new InterviewAnswer(questionId, memberId, content);
    }

    public void updateContent(String content) {
        setContent(content);
    }

    public boolean isAnsweredBy(final long memberId) {
        return this.memberId.equals(memberId);
    }

    private void setQuestionId(@NonNull Long questionId) {
        this.questionId = questionId;
    }

    private void setMemberId(@NonNull Long memberId) {
        this.memberId = memberId;
    }

    private void setContent(@NonNull String content) {
        if (content.isBlank()) {
            throw new DeeppleException(InterviewErrorCode.INVALID_INTERVIEW_ANSWER_CONTENT);
        }
        this.content = content;
    }
}
