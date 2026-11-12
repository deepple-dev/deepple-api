package kr.co.deepple.admin.fixture;

import kr.co.deepple.admin.command.domain.admin.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminFixture {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Long id;
    private String email;
    private String hashedPassword = "hashed-password";
    private String name = "관리자";
    private String phoneNumber = "01099990000";
    private String comment = "관리자 가입 요청";
    private AdminRole role = AdminRole.ADMIN;
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    public static AdminFixture builder() {
        return new AdminFixture();
    }

    public Admin build() {
        Admin admin = Admin.builder()
            .email(Email.from(email == null ? "admin" + SEQUENCE.incrementAndGet() + "@deepple.com" : email))
            .password(Password.fromHashed(hashedPassword))
            .name(Name.from(name))
            .phoneNumber(PhoneNumber.from(phoneNumber))
            .comment(comment)
            .build();
        setField(admin, "id", id);
        setField(admin, "role", role);
        setField(admin, "approvalStatus", approvalStatus);
        return admin;
    }
}
