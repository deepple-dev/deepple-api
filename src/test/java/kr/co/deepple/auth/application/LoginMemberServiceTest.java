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
import kr.co.deepple.auth.application.dto.MemberLoginRequest;
import kr.co.deepple.auth.application.dto.TokenPairResponse;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.application.port.out.spi.MemberRegistrar;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.PhoneNumber;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountLoggedInEvent;
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
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class LoginMemberServiceTest {

  private static final long ACCOUNT_ID = 42L;
  private static final String PHONE_NUMBER = "01012345678";
  private static final MemberLoginRequest REQUEST = new MemberLoginRequest(PHONE_NUMBER, "123456");

  @Mock private MemberAccountRepository memberAccountRepository;

  @Mock private AuthMessageService authMessageService;

  @Mock private TokenProvider tokenProvider;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private MemberRegistrar memberRegistrar;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private LoginMemberService loginMemberService;

  @Test
  @DisplayName("계정이 있으면 토큰을 발급하고 로그인 이벤트만 발행한다.")
  void loginExistingAccount() {
    givenExistingAccount(MemberAccountFixture.builder().id(ACCOUNT_ID).build());
    givenTokens();

    TokenPairResponse response = loginMemberService.login(REQUEST);

    assertThat(response).isEqualTo(new TokenPairResponse("access-token", "refresh-token"));
    verify(authMessageService).authenticate(PHONE_NUMBER, "123456");
    verify(memberAccountRepository, never()).save(any());
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue())
        .isInstanceOfSatisfying(
            MemberAccountLoggedInEvent.class,
            event -> assertThat(event.accountId()).isEqualTo(ACCOUNT_ID));
  }

  @Test
  @DisplayName("계정이 없으면 계정을 만들고 회원 등록을 요청한 뒤 로그인 이벤트를 발행한다.")
  void registerAndLoginNewAccount() {
    when(memberAccountRepository.findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER)))
        .thenReturn(Optional.empty());
    when(memberAccountRepository.save(any(MemberAccount.class)))
        .thenReturn(
            MemberAccountFixture.builder().id(ACCOUNT_ID).phoneNumber(PHONE_NUMBER).build());
    givenTokens();

    loginMemberService.login(REQUEST);

    verify(memberRegistrar).register(ACCOUNT_ID, PHONE_NUMBER);
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(MemberAccountLoggedInEvent.class);
  }

  @Test
  @DisplayName("정지된 계정이면 예외를 던지고 토큰을 발급하지 않는다.")
  void rejectSuspendedAccount() {
    givenExistingAccount(
        MemberAccountFixture.builder()
            .id(ACCOUNT_ID)
            .status(MemberAccountStatus.SUSPENDED_PERMANENTLY)
            .build());

    assertThatThrownBy(() -> loginMemberService.login(REQUEST))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
    verifyNoInteractions(tokenProvider, refreshTokenService, eventPublisher, memberRegistrar);
  }

  @Test
  @DisplayName("같은 전화번호의 가입이 동시에 들어와 unique 제약에 걸리면 동시성 예외를 던진다.")
  void throwConcurrencyFailureWhenPhoneNumberDuplicated() {
    when(memberAccountRepository.findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER)))
        .thenReturn(Optional.empty());
    when(memberAccountRepository.save(any(MemberAccount.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    assertThatThrownBy(() -> loginMemberService.login(REQUEST))
        .isInstanceOf(ConcurrencyFailureException.class);
  }

  private void givenExistingAccount(MemberAccount account) {
    when(memberAccountRepository.findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER)))
        .thenReturn(Optional.of(account));
  }

  private void givenTokens() {
    when(tokenProvider.createAccessToken(eq(ACCOUNT_ID), eq(Role.MEMBER), any(Instant.class)))
        .thenReturn("access-token");
    when(refreshTokenService.issue(TokenOwner.member(ACCOUNT_ID))).thenReturn("refresh-token");
  }
}
