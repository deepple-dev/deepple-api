package kr.co.deepple.admin.domain;

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
@Table(name = "suspensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suspension extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long adminId;

  private long memberId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private SuspensionStatus status;

  /**
   * 일시 정지 만료 시각 - status == TEMPORARY인 경우, 이 시각이 지나면 자동 해제(=정지 엔티티 삭제)한다
   * (SuspensionReleaseScheduler, 로그인·토큰 재발급). - status == PERMANENT인 경우 null이다.
   */
  private Instant expireAt;

  private Suspension(long adminId, long memberId, SuspensionStatus status, Instant expireAt) {
    this.adminId = adminId;
    this.memberId = memberId;
    this.status = status;
    this.expireAt = expireAt;
  }

  public static Suspension createTemporary(long adminId, long memberId) {
    return new Suspension(
        adminId,
        memberId,
        SuspensionStatus.TEMPORARY,
        Instant.now().plus(SuspensionPolicy.TEMPORARY_SUSPENSION_DURATION));
  }

  public static Suspension createPermanent(long adminId, long memberId) {
    return new Suspension(adminId, memberId, SuspensionStatus.PERMANENT, null);
  }

  /** 일시 정지이고 만료 시각이 지났으면 true. 영구 정지는 만료되지 않는다. */
  public boolean isExpired(@NonNull Instant now) {
    return status == SuspensionStatus.TEMPORARY && expireAt != null && !expireAt.isAfter(now);
  }

  /** 만료된 일시 정지면 지금부터 다시 일시 정지한다. 바뀌었으면 true. */
  public boolean restartTemporaryIfExpired(long adminId, @NonNull Instant now) {
    if (!isExpired(now)) {
      return false;
    }
    this.adminId = adminId;
    this.expireAt = now.plus(SuspensionPolicy.TEMPORARY_SUSPENSION_DURATION);
    return true;
  }

  public boolean changeToPermanent(long adminId) {
    if (status == SuspensionStatus.PERMANENT) {
      return false;
    }
    this.adminId = adminId;
    this.status = SuspensionStatus.PERMANENT;
    this.expireAt = null;
    return true;
  }
}
