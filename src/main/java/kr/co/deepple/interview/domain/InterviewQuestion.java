package kr.co.deepple.interview.domain;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "interview_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private InterviewCategory category;

  private boolean isPublic;

  private InterviewQuestion(String content, @NonNull InterviewCategory category, boolean isPublic) {
    setContent(content);
    this.category = category;
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

  public void update(String content, @NonNull InterviewCategory category, boolean isPublic) {
    setContent(content);
    this.category = category;
    this.isPublic = isPublic;
  }
}
