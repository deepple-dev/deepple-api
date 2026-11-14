package kr.co.deepple.auth.application;

import java.time.Instant;
import kr.co.deepple.auth.application.port.in.SuspendMemberAccountUseCase;
import kr.co.deepple.auth.application.port.in.UnsuspendMemberAccountUseCase;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.TokenOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuspendMemberAccountService
    implements SuspendMemberAccountUseCase, UnsuspendMemberAccountUseCase {

  private final MemberAccountRepository memberAccountRepository;
  private final RefreshTokenService refreshTokenService;

  @Override
  @Transactional
  public void suspendTemporarily(long memberId, Instant suspendedUntil) {
    memberAccountRepository
        .findByIdForUpdate(memberId)
        .ifPresent(account -> account.suspendTemporarily(suspendedUntil));
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
  }

  @Override
  @Transactional
  public void suspendPermanently(long memberId) {
    memberAccountRepository
        .findByIdForUpdate(memberId)
        .ifPresent(MemberAccount::suspendPermanently);
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
  }

  @Override
  @Transactional
  public void unsuspend(long memberId) {
    memberAccountRepository.findByIdForUpdate(memberId).ifPresent(MemberAccount::unsuspend);
  }
}
