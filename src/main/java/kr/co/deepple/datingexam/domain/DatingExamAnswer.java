package kr.co.deepple.datingexam.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class DatingExamAnswer extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long questionId;

  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private AnswerPersonalityType personalityType;

  private DatingExamAnswer(Long questionId, String content, AnswerPersonalityType personalityType) {
    setQuestionId(questionId);
    setContent(content);
    setPersonalityType(personalityType);
  }

  public static DatingExamAnswer create(
      Long questionId, String content, AnswerPersonalityType personalityType) {
    return new DatingExamAnswer(questionId, content, personalityType);
  }

  private void setQuestionId(@NonNull Long questionId) {
    this.questionId = questionId;
  }

  private void setContent(@NonNull String content) {
    if (content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be null or blank");
    }
    this.content = content;
  }

  private void setPersonalityType(@NonNull AnswerPersonalityType personalityType) {
    this.personalityType = personalityType;
  }
}
