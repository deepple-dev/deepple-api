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
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded private Email email;

  @Embedded private Password password;

  @Embedded private Name name;

  @Embedded private PhoneNumber phoneNumber;

  private String comment;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private AdminRole role;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ApprovalStatus approvalStatus;

  @Builder
  private Admin(
      @NonNull Email email,
      @NonNull Password password,
      @NonNull Name name,
      @NonNull PhoneNumber phoneNumber,
      String comment) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.comment = comment;
    this.role = AdminRole.ADMIN;
    this.approvalStatus = ApprovalStatus.PENDING;
  }

  public void matchPassword(String rawPassword, PasswordHasher passwordHasher) {
    if (!password.matches(rawPassword, passwordHasher)) {
      throw new DeeppleException(
          AuthErrorCode.INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다. adminId=" + id);
    }
  }

  public void approve(@NonNull AdminRole role) {
    if (approvalStatus != ApprovalStatus.PENDING) {
      throw new DeeppleException(
          AuthErrorCode.ADMIN_NOT_PENDING, "adminId=" + id + ", approvalStatus=" + approvalStatus);
    }

    this.role = role;
    this.approvalStatus = ApprovalStatus.APPROVED;
  }

  public boolean isApproved() {
    return approvalStatus == ApprovalStatus.APPROVED;
  }

  public Role getTokenRole() {
    return role.getTokenRole();
  }
}
