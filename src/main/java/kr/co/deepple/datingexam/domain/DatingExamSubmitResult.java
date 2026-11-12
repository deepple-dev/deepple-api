package kr.co.deepple.datingexam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dating_exam_submit_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingExamSubmitResult extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long memberId;

  @Column(nullable = false)
  private int decisiveIndependentCount;

  @Column(nullable = false)
  private int growingRunningMateCount;

  @Column(nullable = false)
  private int devotedRomanticCount;

  @Column(nullable = false)
  private int realisticShelterCount;

  @Column(nullable = false)
  private int stimulatingAdventurerCount;

  @Column(nullable = false)
  private int rationalRealistCount;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private AnswerPersonalityType dominantPersonalityType;

  private DatingExamSubmitResult(long memberId) {
    this.memberId = memberId;
    this.decisiveIndependentCount = 0;
    this.growingRunningMateCount = 0;
    this.devotedRomanticCount = 0;
    this.realisticShelterCount = 0;
    this.stimulatingAdventurerCount = 0;
    this.rationalRealistCount = 0;
    this.dominantPersonalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;
  }

  public static DatingExamSubmitResult from(long memberId) {
    return new DatingExamSubmitResult(memberId);
  }

  public void addCounts(Map<AnswerPersonalityType, Integer> counts) {
    this.decisiveIndependentCount +=
        counts.getOrDefault(AnswerPersonalityType.DECISIVE_INDEPENDENT, 0);
    this.growingRunningMateCount +=
        counts.getOrDefault(AnswerPersonalityType.GROWING_RUNNING_MATE, 0);
    this.devotedRomanticCount += counts.getOrDefault(AnswerPersonalityType.DEVOTED_ROMANTIC, 0);
    this.realisticShelterCount += counts.getOrDefault(AnswerPersonalityType.REALISTIC_SHELTER, 0);
    this.stimulatingAdventurerCount +=
        counts.getOrDefault(AnswerPersonalityType.STIMULATING_ADVENTURER, 0);
    this.rationalRealistCount += counts.getOrDefault(AnswerPersonalityType.RATIONAL_REALIST, 0);
    recalculateDominant();
  }

  private void recalculateDominant() {
    // 동률 시 우선순위: 자기주도형 > 자극모험형 > 동반성장형 > 현실안정형 > 이성중심형 > 정서교감형
    AnswerPersonalityType dominant = AnswerPersonalityType.DECISIVE_INDEPENDENT;
    int maxCount = this.decisiveIndependentCount;

    if (this.stimulatingAdventurerCount > maxCount) {
      dominant = AnswerPersonalityType.STIMULATING_ADVENTURER;
      maxCount = this.stimulatingAdventurerCount;
    }
    if (this.growingRunningMateCount > maxCount) {
      dominant = AnswerPersonalityType.GROWING_RUNNING_MATE;
      maxCount = this.growingRunningMateCount;
    }
    if (this.realisticShelterCount > maxCount) {
      dominant = AnswerPersonalityType.REALISTIC_SHELTER;
      maxCount = this.realisticShelterCount;
    }
    if (this.rationalRealistCount > maxCount) {
      dominant = AnswerPersonalityType.RATIONAL_REALIST;
      maxCount = this.rationalRealistCount;
    }
    if (this.devotedRomanticCount > maxCount) {
      dominant = AnswerPersonalityType.DEVOTED_ROMANTIC;
    }

    this.dominantPersonalityType = dominant;
  }
}
