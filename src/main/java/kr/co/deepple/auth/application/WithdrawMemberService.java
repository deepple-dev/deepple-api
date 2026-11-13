package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.port.in.WithdrawMemberUseCase;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountDeletedEvent;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawMemberService implements WithdrawMemberUseCase {

  private final MemberAccountRepository memberAccountRepository;
  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void withdraw(long memberId) {
    MemberAccount account =
        memberAccountRepository
            .findByIdForUpdate(memberId)
            .orElseThrow(() ->
                new DeeppleException(AuthErrorCode.MEMBER_NOT_FOUND, "accountId=" + memberId));
    account.delete();
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
    eventPublisher.publishEvent(MemberAccountDeletedEvent.from(memberId));
    log.info("회원 탈퇴");
  }
}
