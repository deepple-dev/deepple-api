package deepple.deepple.admin.command.application.admin;

import deepple.deepple.admin.command.domain.admin.Admin;
import deepple.deepple.admin.command.domain.admin.AdminCommandRepository;
import deepple.deepple.admin.command.domain.admin.Email;
import deepple.deepple.admin.command.domain.admin.PasswordHasher;
import deepple.deepple.admin.command.domain.admin.exception.InvalidPasswordException;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginResponse;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupResponse;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.auth.domain.TokenRepository;
import deepple.deepple.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminCommandRepository adminCommandRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private AdminAuthService adminAuthService;

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
        @DisplayName("이미 존재하는 이메일로 회원가입을 시도하면 DuplicateEmailException이 발생합니다.")
        void throwDuplicateEmailExceptionWhenEmailAlreadyExists() {
            // given
            String email = "exists@example.com";
            AdminSignupRequest request = new AdminSignupRequest(email, "password123^^", "홍길동", "01012345678");

            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(mock(Admin.class)));

            // when & then
            assertThatThrownBy(() -> adminAuthService.signup(request)).isInstanceOf(DuplicateEmailException.class);

            verify(adminCommandRepository, never()).save(any(Admin.class));
            verify(passwordHasher, never()).hash(anyString());
        }

        @Test
        @DisplayName("비밀번호 규칙을 만족하지 못하면 InvalidPasswordException이 발생합니다.")
        void throwInvalidPasswordExceptionWhenPasswordIsInvalid() {
            // given
            String email = "test2@example.com";
            String invalidPassword = "short12^^";
            AdminSignupRequest request = new AdminSignupRequest(email, invalidPassword, "홍길동", "01012345678");

            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminAuthService.signup(request)).isInstanceOf(InvalidPasswordException.class);

            verify(adminCommandRepository).findByEmail(Email.from(email));
            verify(passwordHasher, never()).hash(anyString());
            verify(adminCommandRepository, never()).save(any(Admin.class));
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Mock
        private Admin admin;

        @Test
        @DisplayName("유효한 이메일과 비밀번호로 로그인하면 access token과 refresh token을 발급받고, refresh token은 저장소에 저장합니다.")
        void issueTokensWhenLoginIsValid() {
            // given
            String email = "valid@example.com";
            String requestPassword = "password123^^";
            AdminLoginRequest request = new AdminLoginRequest(email, requestPassword);

            when(admin.getId()).thenReturn(1L);
            when(admin.isApproved()).thenReturn(true);
            when(admin.getTokenRole()).thenReturn(Role.ADMIN);
            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));

            String newAccessToken = "accessToken";
            String newRefreshToken = "refreshToken";
            when(tokenProvider.createAccessToken(eq(1L), eq(Role.ADMIN), any(Instant.class)))
                .thenReturn(newAccessToken);
            when(tokenProvider.createRefreshToken(eq(1L), eq(Role.ADMIN), any(Instant.class)))
                .thenReturn(newRefreshToken);

            // when
            AdminLoginResponse response = adminAuthService.login(request);

            // then
            verify(tokenRepository).save(newRefreshToken);
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(newAccessToken);
            assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인을 시도하면 AdminNotFoundException이 발생합니다.")
        void loginThrowsAdminNotFoundException() {
            // given
            String email = "invalid@example.com";
            String requestPassword = "password123^^";
            AdminLoginRequest request = new AdminLoginRequest(email, requestPassword);

            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminAuthService.login(request)).isInstanceOf(AdminNotFoundException.class);
        }

        @Test
        @DisplayName("삭제된 관리자가 로그인을 시도하면 AdminNotFoundException이 발생합니다.")
        void loginThrowsAdminNotFoundExceptionWhenDeleted() {
            // given
            String email = "deleted@example.com";
            AdminLoginRequest request = new AdminLoginRequest(email, "password123^^");
            when(admin.isDeleted()).thenReturn(true);
            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));

            // when & then
            assertThatThrownBy(() -> adminAuthService.login(request)).isInstanceOf(AdminNotFoundException.class);
            verify(tokenRepository, never()).save(anyString());
        }

        @Test
        @DisplayName("승인되지 않은 관리자가 로그인하면 AdminNotApprovedException이 발생하고 토큰을 발급하지 않습니다.")
        void loginThrowsAdminNotApprovedExceptionWhenPending() {
            // given
            String email = "pending@example.com";
            AdminLoginRequest request = new AdminLoginRequest(email, "password123^^");
            when(admin.isApproved()).thenReturn(false);
            when(adminCommandRepository.findByEmail(Email.from(email))).thenReturn(Optional.of(admin));

            // when & then
            assertThatThrownBy(() -> adminAuthService.login(request)).isInstanceOf(AdminNotApprovedException.class);
            verify(tokenRepository, never()).save(anyString());
        }
    }
}
