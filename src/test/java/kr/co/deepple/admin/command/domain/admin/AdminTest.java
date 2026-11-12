package kr.co.deepple.admin.command.domain.admin;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.fixture.AdminFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTest {

    @Mock
    private PasswordHasher passwordHasher;

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "name", "phoneNumber"})
    @DisplayName("Admin의 값 타입이 null이면 NullPointerException이 발생합니다.")
    void createAdminWithNullValueTypeThrowsException(String fieldName) {
        // given
        Email email = Email.from("example@me.com");
        Password password = Password.fromHashed("hashed-password");
        Name name = Name.from("홍길동");
        PhoneNumber phoneNumber = PhoneNumber.from("01012345678");

        // when & then
        assertThatThrownBy(() ->
            Admin.builder()
                .email(fieldName.equals("email") ? null : email)
                .password(fieldName.equals("password") ? null : password)
                .name(fieldName.equals("name") ? null : name)
                .phoneNumber(fieldName.equals("phoneNumber") ? null : phoneNumber)
                .build())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 PasswordMismatchException이 발생합니다.")
    void throwsPasswordMismatchExceptionWhenPasswordMismatch() {
        // given
        Admin admin = AdminFixture.builder()
            .hashedPassword("hashed-password")
            .build();

        String rawPassword = "raw-password";
        String hashedPassword = "hashed-password";
        when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> admin.matchPassword(rawPassword, passwordHasher))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("가입한 관리자는 승인되지 않은 상태입니다.")
    void newAdminIsNotApproved() {
        // given
        Admin admin = Admin.builder()
            .email(Email.from("example@me.com"))
            .password(Password.fromHashed("hashed-password"))
            .name(Name.from("홍길동"))
            .phoneNumber(PhoneNumber.from("01012345678"))
            .build();

        // when & then
        assertThat(admin.isApproved()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"ADMIN, ADMIN", "SUPER_ADMIN, SUPER_ADMIN"})
    @DisplayName("승인하면 선택한 역할에 맞는 토큰 역할을 갖습니다.")
    void approveWithRole(AdminRole adminRole, Role tokenRole) {
        // given
        Admin admin = AdminFixture.builder()
            .role(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .build();

        // when
        admin.approve(adminRole);

        // then
        assertThat(admin.isApproved()).isTrue();
        assertThat(admin.getTokenRole()).isEqualTo(tokenRole);
    }

    @Test
    @DisplayName("이미 승인된 관리자를 다시 승인하면 예외가 발생합니다.")
    void approveAlreadyApprovedAdmin() {
        // given
        Admin admin = AdminFixture.builder()
            .role(AdminRole.ADMIN)
            .approvalStatus(ApprovalStatus.APPROVED)
            .build();

        // when & then
        assertThatThrownBy(() -> admin.approve(AdminRole.SUPER_ADMIN))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.ADMIN_NOT_PENDING);
    }
}
