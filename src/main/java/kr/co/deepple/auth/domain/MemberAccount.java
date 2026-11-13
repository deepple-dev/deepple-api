package kr.co.deepple.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "member_accounts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberAccount extends SoftDeleteBaseEntity {

  public static final String SUSPENSION_EXPIRE_AT_PROPERTY = "suspensionExpireAt";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded private PhoneNumber phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private MemberAccountStatus status;

  private Instant suspendedUntil;

  public static MemberAccount register(@NonNull String phoneNumber) {
    return MemberAccount.builder()
        .phoneNumber(PhoneNumber.from(phoneNumber))
        .status(MemberAccountStatus.ACTIVE)
        .build();
  }

  public void suspendTemporarily(@NonNull Instant suspendedUntil) {
    this.status = MemberAccountStatus.SUSPENDED_TEMPORARILY;
    this.suspendedUntil = suspendedUntil;
  }

  public void suspendPermanently() {
    this.status = MemberAccountStatus.SUSPENDED_PERMANENTLY;
    this.suspendedUntil = null;
  }

  public void unsuspend() {
    this.status = MemberAccountStatus.ACTIVE;
    this.suspendedUntil = null;
  }

  public void validateLoginAllowed(@NonNull Instant now) {
    if (isDeleted()) {
      throw new DeeppleException(AuthErrorCode.MEMBER_DELETED, "accountId=" + id);
    }
    if (status == MemberAccountStatus.SUSPENDED_PERMANENTLY) {
      throw new DeeppleException(AuthErrorCode.MEMBER_PERMANENTLY_SUSPENDED, "accountId=" + id);
    }
    if (status == MemberAccountStatus.SUSPENDED_TEMPORARILY && suspendedUntil.isAfter(now)) {
      throw new DeeppleException(AuthErrorCode.MEMBER_TEMPORARILY_SUSPENDED, "accountId=" + id)
          .withProperty(
              SUSPENSION_EXPIRE_AT_PROPERTY,
              LocalDateTime.ofInstant(suspendedUntil, ZoneId.systemDefault()));
    }
  }
}
