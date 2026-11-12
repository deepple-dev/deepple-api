package kr.co.deepple.datingexam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "dating_exam_submit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingExamSubmit extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long memberId;

  @Column(nullable = false)
  private long subjectId;

  @Column(nullable = false)
  private String answers;

  private DatingExamSubmit(long memberId, long subjectId, @NonNull String answers) {
    if (answers.isBlank()) {
      throw new IllegalArgumentException("연애고사 답변은 비어있을 수 없습니다.");
    }
    this.memberId = memberId;
    this.subjectId = subjectId;
    this.answers = answers;
  }

  public static DatingExamSubmit of(
      DatingExamSubmitRequest request, DatingExamAnswerEncoder answerEncoder, long memberId) {
    long subjectId = request.subjectId();
    String encodedAnswers = answerEncoder.encode(request);
    return new DatingExamSubmit(memberId, subjectId, encodedAnswers);
  }
}
