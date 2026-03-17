package deepple.deepple.datingexam.domain;

import deepple.deepple.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_dating_exam_submit_result_member",
        columnNames = {"memberId"}
    )
)
public class DatingExamSubmitResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int decisiveIndependentCount;

    @Column(nullable = false)
    private int growingRunningMateCount;

    @Column(nullable = false)
    private int devotedRomanticCount;

    @Column(nullable = false)
    private int realisticShelterCount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private AnswerPersonalityType dominantPersonalityType;

    private DatingExamSubmitResult(@NonNull Long memberId) {
        this.memberId = memberId;
        this.decisiveIndependentCount = 0;
        this.growingRunningMateCount = 0;
        this.devotedRomanticCount = 0;
        this.realisticShelterCount = 0;
        this.dominantPersonalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;
    }

    public static DatingExamSubmitResult create(Long memberId) {
        return new DatingExamSubmitResult(memberId);
    }

    public void addCounts(Map<AnswerPersonalityType, Integer> counts) {
        this.decisiveIndependentCount += counts.getOrDefault(AnswerPersonalityType.DECISIVE_INDEPENDENT, 0);
        this.growingRunningMateCount += counts.getOrDefault(AnswerPersonalityType.GROWING_RUNNING_MATE, 0);
        this.devotedRomanticCount += counts.getOrDefault(AnswerPersonalityType.DEVOTED_ROMANTIC, 0);
        this.realisticShelterCount += counts.getOrDefault(AnswerPersonalityType.REALISTIC_SHELTER, 0);
        recalculateDominant();
    }

    private void recalculateDominant() {
        AnswerPersonalityType dominant = AnswerPersonalityType.DECISIVE_INDEPENDENT;
        int maxCount = this.decisiveIndependentCount;

        if (this.growingRunningMateCount > maxCount) {
            dominant = AnswerPersonalityType.GROWING_RUNNING_MATE;
            maxCount = this.growingRunningMateCount;
        }
        if (this.devotedRomanticCount > maxCount) {
            dominant = AnswerPersonalityType.DEVOTED_ROMANTIC;
            maxCount = this.devotedRomanticCount;
        }
        if (this.realisticShelterCount > maxCount) {
            dominant = AnswerPersonalityType.REALISTIC_SHELTER;
        }

        this.dominantPersonalityType = dominant;
    }
}
