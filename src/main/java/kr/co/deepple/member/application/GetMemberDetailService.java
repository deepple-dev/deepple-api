package kr.co.deepple.member.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.domain.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMemberDetailService implements GetMemberDetailUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public MemberDetail getMemberDetail(long memberId) {
    return memberQueryRepository
        .findDetailByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }
}
