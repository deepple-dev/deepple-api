package kr.co.deepple.auth.application;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.auth.application.dto.MemberLoginRequest;
import kr.co.deepple.auth.application.dto.TokenPairResponse;
import kr.co.deepple.auth.application.port.in.LoginMemberUseCase;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.application.port.out.spi.MemberRegistrar;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.PhoneNumber;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountLoggedInEvent;
import kr.co.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginMemberService implements LoginMemberUseCase {

  private final MemberAccountRepository memberAccountRepository;
  private final AuthMessageService authMessageService;
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final MemberRegistrar memberRegistrar;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public TokenPairResponse login(MemberLoginRequest request) {
    authMessageService.authenticate(request.phoneNumber(), request.code());

    Optional<MemberAccount> existingAccount =
        memberAccountRepository.findByPhoneNumber(PhoneNumber.from(request.phoneNumber()));
    MemberAccount account = existingAccount.orElseGet(() -> register(request.phoneNumber()));
    Instant now = Instant.now();
    account.validateLoginAllowed(now);

    String accessToken = tokenProvider.createAccessToken(account.getId(), Role.MEMBER, now);
    String refreshToken = refreshTokenService.issue(TokenOwner.member(account.getId()));
    eventPublisher.publishEvent(MemberAccountLoggedInEvent.from(account.getId()));

    log.atInfo()
        .addKeyValue("memberId", account.getId())
        .addKeyValue("newMember", existingAccount.isEmpty())
        .log("회원 로그인");

    return new TokenPairResponse(accessToken, refreshToken);
  }

  private MemberAccount register(String phoneNumber) {
    MemberAccount account = saveAccount(phoneNumber);
    memberRegistrar.register(account.getId(), phoneNumber);
    return account;
  }

  private MemberAccount saveAccount(String phoneNumber) {
    try {
      return memberAccountRepository.save(MemberAccount.register(phoneNumber));
    } catch (DataIntegrityViolationException e) {
      throw new ConcurrencyFailureException("같은 전화번호로 회원 가입이 동시에 요청되었습니다.", e);
    }
  }
}
