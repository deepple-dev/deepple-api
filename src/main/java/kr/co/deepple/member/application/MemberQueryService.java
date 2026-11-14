package kr.co.deepple.member.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.domain.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

  private final MemberQueryRepository memberQueryRepository;

  public MemberProfileView getProfile(long memberId) {
    return memberQueryRepository
        .findProfileByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  public MemberContactView getContact(long memberId) {
    return memberQueryRepository
        .findContactsByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }
}
