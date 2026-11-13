package kr.co.deepple.auth.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.auth.application.dto.AdminLoginRequest;
import kr.co.deepple.auth.application.dto.AdminLoginResponse;
import kr.co.deepple.auth.application.dto.AdminSignupRequest;
import kr.co.deepple.auth.application.dto.AdminSignupResponse;
import kr.co.deepple.auth.application.port.out.AdminCommandRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.Email;
import kr.co.deepple.auth.domain.PasswordHasher;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.fixture.AdminFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

  private static final String HASHED_PASSWORD = "hashed-password123^^";

  @Mock private AdminCommandRepository adminCommandRepository;

  @Mock private PasswordHasher passwordHasher;

  @Mock private TokenProvider tokenProvider;

  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private AdminAuthService adminAuthService;

  @Nested
  @DisplayName("회원가입")
  class Signup {

    @Test
    @DisplayName("중복되지 않은 이메일과 유효한 비밀번호로 회원가입 할 수 있습니다.")
    void canSignupWhenRequestIsValid() {
      // given
      String email = "test@example.com";
      String rawPassword = "password123^^";
      AdminSignupRequest request = new AdminSignupRequest(email, rawPassword, "홍길동", "01012345678");

      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.empty());
      when(passwordHasher.hash(rawPassword)).thenReturn("hashed-password123^^");
      when(adminCommandRepository.save(any(Admin.class)))
          .thenAnswer(invocation -> {
            Admin admin = invocation.getArgument(0, Admin.class);
            setField(admin, "id", 1L);
            return admin;
          });

      // when
      AdminSignupResponse response = adminAuthService.signup(request);

      // then
      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(1L);
      assertThat(response.email()).isEqualTo(email);

      verify(adminCommandRepository).findByEmail(Email.from(email));
      verify(passwordHasher).hash(rawPassword);
      verify(adminCommandRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입을 시도하면 ADMIN_EMAIL_ALREADY_EXISTS 예외가 발생합니다.")
    void throwAdminEmailAlreadyExistsWhenEmailAlreadyExists() {
      // given
      String email = "exists@example.com";
      AdminSignupRequest request =
          new AdminSignupRequest(email, "password123^^", "홍길동", "01012345678");

      when(adminCommandRepository.findByEmail(Email.from(email)))
          .thenReturn(Optional.of(AdminFixture.builder().build()));

      // when & then
      assertThatThrownBy(() -> adminAuthService.signup(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.ADMIN_EMAIL_ALREADY_EXISTS);

      verify(adminCommandRepository, never()).save(any(Admin.class));
      verify(passwordHasher, never()).hash(anyString());
    }

    @Test
    @DisplayName("비밀번호 규칙을 만족하지 못하면 INVALID_PASSWORD 예외가 발생합니다.")
    void throwInvalidPasswordWhenPasswordIsInvalid() {
      // given
      String email = "test2@example.com";
      String invalidPassword = "short12^^";
      AdminSignupRequest request =
          new AdminSignupRequest(email, invalidPassword, "홍길동", "01012345678");

      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> adminAuthService.signup(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_PASSWORD);

      verify(adminCommandRepository).findByEmail(Email.from(email));
      verify(passwordHasher, never()).hash(anyString());
      verify(adminCommandRepository, never()).save(any(Admin.class));
    }
  }

  @Nested
  @DisplayName("로그인")
  class Login {

    @Test
    @DisplayName(
        "유효한 이메일과 비밀번호로 로그인하면 access token과 refresh token을 발급받고, refresh token은 관리자 역할로 발급합니다.")
    void issueTokensWhenLoginIsValid() {
      // given
      String email = "valid@example.com";
      String requestPassword = "password123^^";
      AdminLoginRequest request = new AdminLoginRequest(email, requestPassword);

      Admin admin =
          AdminFixture.builder()
              .id(1L)
              .email(email)
              .hashedPassword(HASHED_PASSWORD)
              .role(AdminRole.ADMIN)
              .approvalStatus(ApprovalStatus.APPROVED)
              .build();
      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));
      when(passwordHasher.matches(requestPassword, HASHED_PASSWORD)).thenReturn(true);

      String newAccessToken = "accessToken";
      String newRefreshToken = "refreshToken";
      when(tokenProvider.createAccessToken(eq(1L), eq(Role.ADMIN), any(Instant.class)))
          .thenReturn(newAccessToken);
      when(refreshTokenService.issue(new TokenOwner(1L, Role.ADMIN))).thenReturn(newRefreshToken);

      // when
      AdminLoginResponse response = adminAuthService.login(request);

      // then
      assertThat(response).isNotNull();
      assertThat(response.accessToken()).isEqualTo(newAccessToken);
      assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인을 시도하면 INVALID_CREDENTIALS 예외가 발생합니다.")
    void loginThrowsInvalidCredentials() {
      // given
      String email = "invalid@example.com";
      String requestPassword = "password123^^";
      AdminLoginRequest request = new AdminLoginRequest(email, requestPassword);

      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> adminAuthService.login(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("삭제된 관리자가 로그인을 시도하면 INVALID_CREDENTIALS 예외가 발생합니다.")
    void loginThrowsInvalidCredentialsWhenDeleted() {
      // given
      String email = "deleted@example.com";
      AdminLoginRequest request = new AdminLoginRequest(email, "password123^^");
      Admin admin = AdminFixture.builder().email(email).build();
      admin.delete();
      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));

      // when & then
      assertThatThrownBy(() -> adminAuthService.login(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
      verify(refreshTokenService, never()).issue(any());
    }

    @Test
    @DisplayName("승인되지 않은 관리자가 로그인하면 ADMIN_NOT_APPROVED 예외가 발생하고 토큰을 발급하지 않습니다.")
    void loginThrowsAdminNotApprovedWhenPending() {
      // given
      String email = "pending@example.com";
      String requestPassword = "password123^^";
      AdminLoginRequest request = new AdminLoginRequest(email, requestPassword);
      Admin admin =
          AdminFixture.builder()
              .email(email)
              .hashedPassword(HASHED_PASSWORD)
              .approvalStatus(ApprovalStatus.PENDING)
              .build();
      when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));
      when(passwordHasher.matches(requestPassword, HASHED_PASSWORD)).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> adminAuthService.login(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.ADMIN_NOT_APPROVED);
      verify(refreshTokenService, never()).issue(any());
    }
  }

  @Nested
  @DisplayName("토큰 갱신")
  class Refresh {

    private static final String REFRESH_TOKEN = "refreshToken";

    @Test
    @DisplayName("승인된 관리자가 갱신하면 현재 역할로 새 토큰을 발급합니다.")
    void issueTokensWithCurrentRole() {
      // given
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(new TokenOwner(1L, Role.ADMIN));
      Admin admin =
          AdminFixture.builder()
              .id(1L)
              .role(AdminRole.SUPER_ADMIN)
              .approvalStatus(ApprovalStatus.APPROVED)
              .build();
      when(adminCommandRepository.findById(1L)).thenReturn(Optional.of(admin));
      when(tokenProvider.createAccessToken(eq(1L), eq(Role.SUPER_ADMIN), any(Instant.class)))
          .thenReturn("accessToken");
      when(refreshTokenService.issue(new TokenOwner(1L, Role.SUPER_ADMIN)))
          .thenReturn("newRefreshToken");

      // when
      AdminLoginResponse response = adminAuthService.refresh(REFRESH_TOKEN);

      // then
      assertThat(response.accessToken()).isEqualTo("accessToken");
      assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("승인되지 않은 관리자가 갱신하면 예외가 발생하고 모든 리프레시 토큰을 폐기합니다.")
    void revokeAllWhenAdminIsNotApproved() {
      // given
      TokenOwner owner = new TokenOwner(1L, Role.ADMIN);
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(owner);
      Admin admin = AdminFixture.builder().id(1L).approvalStatus(ApprovalStatus.PENDING).build();
      when(adminCommandRepository.findById(1L)).thenReturn(Optional.of(admin));

      // when & then
      assertThatThrownBy(() -> adminAuthService.refresh(REFRESH_TOKEN))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
      verify(refreshTokenService).revokeAll(owner);
      verify(refreshTokenService, never()).issue(any());
    }

    @Test
    @DisplayName("회원 리프레시 토큰으로 관리자 갱신을 요청하면 예외가 발생합니다.")
    void rejectMemberRefreshToken() {
      // given
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(TokenOwner.member(1L));

      // when & then
      assertThatThrownBy(() -> adminAuthService.refresh(REFRESH_TOKEN))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
      verify(adminCommandRepository, never()).findById(any());
    }
  }
}
