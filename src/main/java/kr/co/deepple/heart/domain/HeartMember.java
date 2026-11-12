package kr.co.deepple.heart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "heart_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Gender gender;

  private boolean isVip;

  private Instant sourceUpdatedAt;

  private HeartMember(
      long memberId, Gender gender, boolean isVip, @NonNull Instant sourceUpdatedAt) {
    this.memberId = memberId;
    this.gender = gender;
    this.isVip = isVip;
    this.sourceUpdatedAt = sourceUpdatedAt;
  }

  public static HeartMember of(
      long memberId, Gender gender, boolean isVip, Instant sourceUpdatedAt) {
    return new HeartMember(memberId, gender, isVip, sourceUpdatedAt);
  }

  public void update(Gender gender, boolean isVip, @NonNull Instant sourceUpdatedAt) {
    if (!sourceUpdatedAt.isAfter(this.sourceUpdatedAt)) {
      return;
    }
    this.gender = gender;
    this.isVip = isVip;
    this.sourceUpdatedAt = sourceUpdatedAt;
  }
}
