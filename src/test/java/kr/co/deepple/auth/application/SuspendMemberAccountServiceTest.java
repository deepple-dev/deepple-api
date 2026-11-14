package kr.co.deepple.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuspendMemberAccountServiceTest {

  private static final long ACCOUNT_ID = 42L;

  @Mock private MemberAccountRepository memberAccountRepository;

  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private SuspendMemberAccountService suspendMemberAccountService;

  @Test
  @DisplayName("일시 정지하면 계정을 일시 정지 상태로 바꾸고 모든 리프레시 토큰을 폐기한다.")
  void suspendAccountTemporarilyAndRevokeTokens() {
    MemberAccount account = MemberAccountFixture.builder().id(ACCOUNT_ID).build();
    when(memberAccountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));
    Instant suspendedUntil = Instant.parse("2026-11-20T00:00:00Z");

    suspendMemberAccountService.suspendTemporarily(ACCOUNT_ID, suspendedUntil);

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.SUSPENDED_TEMPORARILY);
    assertThat(account.getSuspendedUntil()).isEqualTo(suspendedUntil);
    verify(refreshTokenService).revokeAll(TokenOwner.member(ACCOUNT_ID));
  }

  @Test
  @DisplayName("영구 정지하면 해제 시각 없이 영구 정지 상태로 바꾸고 모든 리프레시 토큰을 폐기한다.")
  void suspendAccountPermanentlyAndRevokeTokens() {
    MemberAccount account =
        MemberAccountFixture.builder()
            .id(ACCOUNT_ID)
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(Instant.parse("2026-11-20T00:00:00Z"))
            .build();
    when(memberAccountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

    suspendMemberAccountService.suspendPermanently(ACCOUNT_ID);

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.SUSPENDED_PERMANENTLY);
    assertThat(account.getSuspendedUntil()).isNull();
    verify(refreshTokenService).revokeAll(TokenOwner.member(ACCOUNT_ID));
  }

  @Test
  @DisplayName("해제하면 계정을 정상 상태로 되돌리고 토큰은 건드리지 않는다.")
  void unsuspendAccount() {
    MemberAccount account =
        MemberAccountFixture.builder()
            .id(ACCOUNT_ID)
            .status(MemberAccountStatus.SUSPENDED_PERMANENTLY)
            .build();
    when(memberAccountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

    suspendMemberAccountService.unsuspend(ACCOUNT_ID);

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.ACTIVE);
    verifyNoInteractions(refreshTokenService);
  }
}
