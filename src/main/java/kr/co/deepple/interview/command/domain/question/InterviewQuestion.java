package kr.co.deepple.interview.command.domain.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "interview_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Getter private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  @Getter
  private InterviewCategory category;

  @Getter private boolean isPublic;

  private InterviewQuestion(String content, InterviewCategory category, boolean isPublic) {
    setContent(content);
    setCategory(category);
    this.isPublic = isPublic;
  }

  public static InterviewQuestion of(String content, InterviewCategory category, boolean isPublic) {
    return new InterviewQuestion(content, category, isPublic);
  }

  private void setContent(@NonNull String content) {
    if (content.isBlank()) {
      throw new DeeppleException(InterviewErrorCode.INVALID_INTERVIEW_QUESTION_CONTENT);
    }
    this.content = content;
  }

  private void setCategory(@NonNull InterviewCategory category) {
    this.category = category;
  }

  public void update(String content, InterviewCategory interviewCategory, boolean isPublic) {
    setContent(content);
    setCategory(interviewCategory);
    this.isPublic = isPublic;
  }
}
