package kr.co.deepple.admin.command.domain.admin;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Embedded
    @Getter
    private Email email;

    @Embedded
    @Getter
    private Password password;

    @Embedded
    @Getter
    private Name name;

    @Embedded
    @Getter
    private PhoneNumber phoneNumber;

    private String comment;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private AdminRole role;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private ApprovalStatus approvalStatus;

    @Builder
    private Admin(Email email, Password password, Name name, PhoneNumber phoneNumber, String comment) {
        setEmail(email);
        setPassword(password);
        setName(name);
        setPhoneNumber(phoneNumber);
        this.comment = comment;
        this.role = AdminRole.ADMIN;
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    private void setEmail(@NonNull Email email) {
        this.email = email;
    }

    private void setPassword(@NonNull Password password) {
        this.password = password;
    }

    private void setName(@NonNull Name name) {
        this.name = name;
    }

    private void setPhoneNumber(@NonNull PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void matchPassword(String rawPassword, PasswordHasher passwordHasher) {
        if (!password.matches(rawPassword, passwordHasher)) {
            throw new DeeppleException(AdminErrorCode.INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다. adminId=" + id);
        }
    }

    public void approve(@NonNull AdminRole role) {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new DeeppleException(
                AdminErrorCode.ADMIN_NOT_PENDING,
                "adminId=" + id + ", approvalStatus=" + approvalStatus
            );
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