package kr.co.deepple.datingexam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "dating_exam_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingExamQuestion extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long subjectId;

  @Column(nullable = false)
  private String content;

  private DatingExamQuestion(long subjectId, @NonNull String content) {
    if (content.isBlank()) {
      throw new IllegalArgumentException("질문 내용은 비어 있을 수 없습니다.");
    }
    this.subjectId = subjectId;
    this.content = content;
  }

  public static DatingExamQuestion of(long subjectId, String content) {
    return new DatingExamQuestion(subjectId, content);
  }
}
