package kr.co.deepple.interview.command.domain.answer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.command.domain.InterviewErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "interview_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswer extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long questionId;

  private long memberId;

  private String content;

  private InterviewAnswer(long questionId, long memberId, String content) {
    this.questionId = questionId;
    this.memberId = memberId;
    setContent(content);
  }

  public static InterviewAnswer of(long questionId, long memberId, String content) {
    return new InterviewAnswer(questionId, memberId, content);
  }

  public void updateContent(String content) {
    setContent(content);
  }

  public boolean isAnsweredBy(long memberId) {
    return this.memberId == memberId;
  }

  private void setContent(@NonNull String content) {
    if (content.isBlank()) {
      throw new DeeppleException(InterviewErrorCode.INVALID_INTERVIEW_ANSWER_CONTENT);
    }
    this.content = content;
  }
}
