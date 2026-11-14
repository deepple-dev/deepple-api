package kr.co.deepple.member.command.application.member;

import kr.co.deepple.auth.application.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

  private final MemberCommandRepository memberCommandRepository;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public void delete(long memberId) {
    Member member = getMemberById(memberId);
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
    member.delete();
    log.info("회원 탈퇴");
  }

  private Member getMemberById(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }
}
