package kr.co.deepple.datingexam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "dating_exam_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingExamAnswer extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long questionId;

  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private AnswerPersonalityType personalityType;

  private DatingExamAnswer(
      long questionId, @NonNull String content, @NonNull AnswerPersonalityType personalityType) {
    if (content.isBlank()) {
      throw new IllegalArgumentException("답변 내용은 비어 있을 수 없습니다.");
    }
    this.questionId = questionId;
    this.content = content;
    this.personalityType = personalityType;
  }

  public static DatingExamAnswer of(
      long questionId, String content, AnswerPersonalityType personalityType) {
    return new DatingExamAnswer(questionId, content, personalityType);
  }
}
