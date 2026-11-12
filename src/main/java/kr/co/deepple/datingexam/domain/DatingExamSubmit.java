package kr.co.deepple.datingexam.domain;

import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_dating_exam_submit_member_subject",
        columnNames = {"memberId", "subjectId"}
    )
)
public class DatingExamSubmit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false)
    private String answers;

    private DatingExamSubmit(@NonNull Long memberId, @NonNull Long subjectId, @NonNull String answers) {
        this.memberId = memberId;
        this.subjectId = subjectId;
        if (answers.isBlank()) {
            throw new IllegalArgumentException("연애고사 답변은 비어있을 수 없습니다.");
        }
        this.answers = answers;
    }

    public static DatingExamSubmit from(
        DatingExamSubmitRequest request,
        DatingExamAnswerEncoder answerEncoder,
        Long memberId
    ) {
        Long subjectId = request.subjectId();
        String encodedAnswers = answerEncoder.encode(request);
        return new DatingExamSubmit(memberId, subjectId, encodedAnswers);
    }
}
