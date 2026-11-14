package kr.co.deepple.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.auth.application.dto.TokenPairResponse;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountTokenRefreshedEvent;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RefreshMemberTokenServiceTest {

  private static final long ACCOUNT_ID = 42L;
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final TokenOwner OWNER = TokenOwner.member(ACCOUNT_ID);

  @Mock private MemberAccountRepository memberAccountRepository;

  @Mock private TokenProvider tokenProvider;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private RefreshMemberTokenService refreshMemberTokenService;

  @Test
  @DisplayName("재발급에 성공하면 새 토큰을 발급하고 재발급 이벤트를 발행한다.")
  void refreshTokens() {
    when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(OWNER);
    when(memberAccountRepository.findById(ACCOUNT_ID))
        .thenReturn(Optional.of(MemberAccountFixture.builder().id(ACCOUNT_ID).build()));
    when(tokenProvider.createAccessToken(eq(ACCOUNT_ID), eq(Role.MEMBER), any(Instant.class)))
        .thenReturn("new-access-token");
    when(refreshTokenService.issue(OWNER)).thenReturn("new-refresh-token");

    TokenPairResponse response = refreshMemberTokenService.refresh(REFRESH_TOKEN);

    assertThat(response).isEqualTo(new TokenPairResponse("new-access-token", "new-refresh-token"));
    ArgumentCaptor<MemberAccountTokenRefreshedEvent> captor =
        ArgumentCaptor.forClass(MemberAccountTokenRefreshedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue().accountId()).isEqualTo(ACCOUNT_ID);
  }

  @Test
  @DisplayName("정지된 계정이 재발급하면 예외를 던지고 모든 리프레시 토큰을 폐기한다.")
  void revokeAllWhenAccountSuspended() {
    when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(OWNER);
    when(memberAccountRepository.findById(ACCOUNT_ID))
        .thenReturn(
            Optional.of(
                MemberAccountFixture.builder()
                    .id(ACCOUNT_ID)
                    .status(MemberAccountStatus.SUSPENDED_PERMANENTLY)
                    .build()));

    assertThatThrownBy(() -> refreshMemberTokenService.refresh(REFRESH_TOKEN))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
    verify(refreshTokenService).revokeAll(OWNER);
    verify(refreshTokenService, never()).issue(any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("계정이 없으면 모든 리프레시 토큰을 폐기하고 유효하지 않은 토큰으로 응답한다.")
  void revokeAllWhenAccountNotFound() {
    when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(OWNER);
    when(memberAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshMemberTokenService.refresh(REFRESH_TOKEN))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
    verify(refreshTokenService).revokeAll(OWNER);
  }

  @Test
  @DisplayName("관리자 리프레시 토큰으로 회원 재발급을 요청하면 유효하지 않은 토큰으로 응답한다.")
  void rejectAdminRefreshToken() {
    when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(new TokenOwner(1L, Role.ADMIN));

    assertThatThrownBy(() -> refreshMemberTokenService.refresh(REFRESH_TOKEN))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
    verifyNoInteractions(memberAccountRepository, tokenProvider, eventPublisher);
  }
}
