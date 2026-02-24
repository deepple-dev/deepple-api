package deepple.deepple.auth.application;

import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.common.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TokenParser tokenParser;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Access token이 null인 경우 MISSING_ACCESS_TOKEN 에러를 반환합니다.")
    void shouldReturnErrorWhenAccessTokenIsNull() {
        // given
        String accessToken = null;

        // when
        AuthResponse response = authService.authenticate(accessToken);

        // then
        assertThat(response.isAuthenticated()).isFalse();
        assertThat(response.getErrorStatus()).isEqualTo(AuthErrorStatus.MISSING_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Access token이 유효한 경우 id와 role을 반환합니다.")
    void shouldReturnIdAndRoleWhenAccessTokenIsValid() {
        // given
        String accessToken = "validAccessToken";

        when(tokenParser.isValid(accessToken)).thenReturn(true);
        when(tokenParser.getId(accessToken)).thenReturn(1L);
        when(tokenParser.getRole(accessToken)).thenReturn(Role.MEMBER);

        // when
        AuthResponse response = authService.authenticate(accessToken);

        // then
        assertThat(response.isAuthenticated()).isTrue();
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    @DisplayName("Access token이 유효하지 않은 경우 INVALID_ACCESS_TOKEN 에러를 반환합니다.")
    void shouldReturnErrorWhenAccessTokenIsInvalid() {
        // given
        String accessToken = "invalidAccessToken";

        when(tokenParser.isValid(accessToken)).thenReturn(false);
        when(tokenParser.isExpired(accessToken)).thenReturn(false);

        // when
        AuthResponse response = authService.authenticate(accessToken);

        // then
        assertThat(response.isAuthenticated()).isFalse();
        assertThat(response.getErrorStatus()).isEqualTo(AuthErrorStatus.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Access token이 만료된 경우 EXPIRED_ACCESS_TOKEN 에러를 반환합니다.")
    void shouldReturnErrorWhenAccessTokenIsExpired() {
        // given
        String accessToken = "expiredAccessToken";

        when(tokenParser.isValid(accessToken)).thenReturn(false);
        when(tokenParser.isExpired(accessToken)).thenReturn(true);

        // when
        AuthResponse response = authService.authenticate(accessToken);

        // then
        assertThat(response.isAuthenticated()).isFalse();
        assertThat(response.getErrorStatus()).isEqualTo(AuthErrorStatus.EXPIRED_ACCESS_TOKEN);
    }
}