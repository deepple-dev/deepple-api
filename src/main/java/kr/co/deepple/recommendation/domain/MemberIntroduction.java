package kr.co.deepple.recommendation.domain;

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
@Table(name = "member_introductions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberIntroduction extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private long introducedMemberId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private IntroductionType type;

  private MemberIntroduction(
      long memberId, long introducedMemberId, @NonNull IntroductionType type) {
    if (memberId == introducedMemberId) {
      throw new DeeppleException(
          RecommendationErrorCode.SELF_MEMBER_INTRODUCTION, "memberId=" + memberId);
    }
    this.memberId = memberId;
    this.introducedMemberId = introducedMemberId;
    this.type = type;
  }

  public static MemberIntroduction of(
      long memberId, long introducedMemberId, IntroductionType type) {
    return new MemberIntroduction(memberId, introducedMemberId, type);
  }
}
