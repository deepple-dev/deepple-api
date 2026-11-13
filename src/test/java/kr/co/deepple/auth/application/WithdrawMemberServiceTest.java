package kr.co.deepple.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountDeletedEvent;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
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
class WithdrawMemberServiceTest {

  private static final long ACCOUNT_ID = 42L;

  @Mock private MemberAccountRepository memberAccountRepository;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private WithdrawMemberService withdrawMemberService;

  @Test
  @DisplayName("탈퇴하면 계정을 삭제 처리하고 모든 리프레시 토큰을 폐기한 뒤 탈퇴 이벤트를 발행한다.")
  void deleteAccountAndRevokeTokens() {
    MemberAccount account = MemberAccountFixture.builder().id(ACCOUNT_ID).build();
    when(memberAccountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

    withdrawMemberService.withdraw(ACCOUNT_ID);

    assertThat(account.isDeleted()).isTrue();
    verify(refreshTokenService).revokeAll(TokenOwner.member(ACCOUNT_ID));
    ArgumentCaptor<MemberAccountDeletedEvent> captor =
        ArgumentCaptor.forClass(MemberAccountDeletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue().accountId()).isEqualTo(ACCOUNT_ID);
  }

  @Test
  @DisplayName("계정이 없으면 회원을 찾을 수 없다는 예외를 던진다.")
  void throwWhenAccountNotFound() {
    when(memberAccountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> withdrawMemberService.withdraw(ACCOUNT_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    verifyNoInteractions(refreshTokenService, eventPublisher);
  }
}
